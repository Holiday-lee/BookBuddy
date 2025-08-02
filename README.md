# BookBuddy - Book Sharing Application

A Spring Boot application for sharing and exchanging books between users.

## Features

- User registration and authentication
- Book listing and management
- Book sharing (Give away, Lend, Swap)
- Real-time chat functionality
- Location-based book search
- Request management system

## Local Development

### Prerequisites
- Java 17
- Maven
- MySQL 8.0

### Setup
1. Clone the repository
2. Create a MySQL database named `bookbuddy`
3. Update `src/main/resources/application.properties` with your database credentials
4. Run `./mvnw spring-boot:run`

## Deployment to Render

### Prerequisites
- Render account
- GitHub repository with your code

### Steps

1. **Push your code to GitHub**
   ```bash
   git add .
   git commit -m "Prepare for Render deployment"
   git push origin main
   ```

2. **Create a new Web Service on Render**
   - Go to [Render Dashboard](https://dashboard.render.com/)
   - Click "New +" → "Web Service"
   - Connect your GitHub repository

3. **Configure the Web Service**
   - **Name**: `bookbuddy` (or your preferred name)
   - **Environment**: `Java`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/bookbuddy-0.0.1-SNAPSHOT.jar`

4. **Add Environment Variables**
   - `SPRING_PROFILES_ACTIVE`: `prod`
   - `SERVER_PORT`: `8080`

5. **Create a PostgreSQL Database**
   - Go to "New +" → "PostgreSQL"
   - Name it `bookbuddy-db`
   - Copy the connection details

6. **Link Database to Web Service**
   - In your web service settings, go to "Environment"
   - Add these environment variables from the database:
     - `SPRING_DATASOURCE_URL` (from database connection string)
     - `SPRING_DATASOURCE_USERNAME` (from database user)
     - `SPRING_DATASOURCE_PASSWORD` (from database password)

7. **Deploy**
   - Click "Create Web Service"
   - Render will automatically build and deploy your application

### Alternative: Using render.yaml

If you prefer using the `render.yaml` file:
1. Ensure your repository contains the `render.yaml` file
2. Go to "New +" → "Blueprint"
3. Connect your GitHub repository
4. Render will automatically create the web service and database

## Environment Variables

### Production (Render)
- `SPRING_PROFILES_ACTIVE`: `prod`
- `SPRING_DATASOURCE_URL`: PostgreSQL connection string
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SERVER_PORT`: `8080`

### Development
- `SPRING_PROFILES_ACTIVE`: `dev`
- Database configuration in `application.properties`

## Database Migration

The application uses JPA with `hibernate.ddl-auto=update`, which will automatically create/update database tables based on your entity classes.

## Security

- CSRF protection enabled
- Session-based authentication
- Secure cookie settings in production
- HTTPS enforced in production

## Monitoring

- Application logs are available in Render dashboard
- Database logs can be viewed in the PostgreSQL service dashboard

## Troubleshooting

### Common Issues

1. **Build fails**: Check that all dependencies are properly declared in `pom.xml`
2. **Database connection fails**: Verify environment variables are correctly set
3. **Application won't start**: Check logs in Render dashboard for specific errors

### Logs

- Application logs: Available in Render web service dashboard
- Database logs: Available in PostgreSQL service dashboard

## Support

For issues with the application, check the logs in Render dashboard or contact the development team. 