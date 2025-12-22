AWS DEPLOYMENT - EVENT SERVICE (EC2 + ECR)

# 1. Prerequisites (one-time)
1.1. Create an AWS IAM user with programmatic access and permissions:
    - AmazonEC2FullAccess
    - AmazonEC2ContainerRegistryFullAccess
    - CloudWatchLogsFullAccess (optional for logs)

1.2. Configure AWS CLI on local machine:
    aws configure
    - AWS Access Key ID: <your-access-key>
    - AWS Secret Access Key: <your-secret-key>
    - Default region: ap-south-1
    - Default output: json

1.3. Ensure Docker is installed and running locally.


# 2. Build Event Service Docker Image Locally
2.1. From project root, build the JAR:
    cd event-service
    ./gradlew clean build -x test

2.2. Build Docker image using the provided Dockerfile:
    docker build -t shadow-ledger-event-service:latest .


# 3. Create ECR Repository and Push Image
3.1. Create an ECR repository for the event service:
    aws ecr create-repository \
      --repository-name shadow-ledger-event-service \
      --region ap-south-1

3.2. Get the AWS account ID (if not already known):
    aws sts get-caller-identity --query Account --output text

3.3. Log in Docker to ECR:
    aws ecr get-login-password --region ap-south-1 | \
      docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com

3.4. Tag the local image with the ECR repository URI:
    docker tag shadow-ledger-event-service:latest \
      <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com/shadow-ledger-event-service:latest

3.5. Push the image to ECR:
    docker push <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com/shadow-ledger-event-service:latest


# 4. Create EC2 Instance (to run the container)
4.1. In AWS Console, go to EC2 → Instances → Launch instance.

4.2. Choose an AMI:
    - Amazon Linux 2 (x86_64)

4.3. Choose instance type:
    - t3.micro (or t2.micro)

4.4. Configure key pair:
    - Create/select a key pair for SSH access.

4.5. Configure security group:
    - Inbound rules:
      * SSH: port 22 from your IP
      * HTTP: port 80 (optional)
      * Custom TCP: port 8081 from your IP (for Event Service)

4.6. Launch the instance.

4.7. Note the Public IPv4 address or Public DNS name of the instance.


# 5. Install Docker on EC2
5.1. SSH into the EC2 instance:
    ssh -i <your-key.pem> ec2-user@<EC2_PUBLIC_IP>

5.2. Install Docker:
    sudo yum update -y
    sudo amazon-linux-extras install docker -y
    sudo service docker start
    sudo usermod -aG docker ec2-user
    # Log out and log in again to apply group changes.

5.3. Verify Docker:
    docker ps


# 6. Pull Event Service Image from ECR on EC2
6.1. Configure AWS CLI inside the EC2 instance (if not already configured):
    aws configure
    - Use same region: ap-south-1

6.2. Log in Docker to ECR from EC2:
    aws ecr get-login-password --region ap-south-1 | \
      docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com

6.3. Pull the event service image:
    docker pull <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com/shadow-ledger-event-service:latest


# 7. Run Event Service Container on EC2 (With Environment Variables)
7.1. Decide environment variables (for evaluation, simple placeholders):

    - SPRING_DATASOURCE_URL
    - SPRING_DATASOURCE_USERNAME
    - SPRING_DATASOURCE_PASSWORD
    - SPRING_KAFKA_BOOTSTRAP_SERVERS

    (For the evaluation, these can point to non-production or placeholder values.
     The key requirement is that the container is started using environment variables.)

7.2. Run the container, mapping container port 8081 to host port 8081:

    docker run -d \
      --name shadow-ledger-event-service \
      -p 8081:8081 \
      -e SPRING_DATASOURCE_URL=jdbc:postgresql://<DB_HOST>:5432/ledger_db \
      -e SPRING_DATASOURCE_USERNAME=ledger_user \
      -e SPRING_DATASOURCE_PASSWORD=ledger_pass \
      -e SPRING_KAFKA_BOOTSTRAP_SERVERS=<KAFKA_BOOTSTRAP_SERVERS> \
      <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com/shadow-ledger-event-service:latest

7.3. Verify that the container is running:
    docker ps

7.4. Check application health from inside EC2:
    curl http://localhost:8081/actuator/health


# 8. Verify Service Is Reachable from Local Machine
8.1. From your local machine, call the health endpoint via EC2 public IP or public DNS:

    curl http://<EC2_PUBLIC_IP>:8081/actuator/health

8.2. Expected response (HTTP 200):
    {"status":"UP", ...}
