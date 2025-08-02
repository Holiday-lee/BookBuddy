# Quick Deployment Guide - BookBuddy to Render

## 🚀 Quick Start

1. **Run the deployment script:**
   ```bash
   ./deploy.sh
   ```

2. **Push to GitHub:**
   ```bash
   git add .
   git commit -m "Prepare for Render deployment"
   git push origin main
   ```

3. **Deploy on Render:**
   - Go to [Render Dashboard](https://dashboard.render.com/)
   - Click "New +" → "Web Service"
   - Connect your GitHub repository
   - Use these settings:
     - **Environment**: Java
     - **Build Command**: `./mvnw clean package -DskipTests`
     - **Start Command**: `java -jar target/bookbuddy-0.0.1-SNAPSHOT.jar`

## 📋 Required Environment Variables

| Variable | Value | Source |
|----------|-------|--------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Manual |
| `SERVER_PORT` | `8080` | Manual |
| `SPRING_DATASOURCE_URL` | Connection string | PostgreSQL DB |
| `SPRING_DATASOURCE_USERNAME` | Username | PostgreSQL DB |
| `SPRING_DATASOURCE_PASSWORD` | Password | PostgreSQL DB |

## 🗄️ Database Setup

1. Create a PostgreSQL database on Render
2. Copy the connection details
3. Add them as environment variables in your web service

## 🔧 Alternative: Using render.yaml

If you prefer automatic setup:
1. Ensure `render.yaml` is in your repository
2. Go to "New +" → "Blueprint"
3. Connect your GitHub repository
4. Render will automatically create everything

## 🐛 Troubleshooting

### Build Issues
- Check that all dependencies are in `pom.xml`
- Verify Java 17 is being used
- Check the build logs in Render dashboard

### Database Issues
- Verify environment variables are correctly set
- Check database connection logs
- Ensure PostgreSQL is running

### Application Issues
- Check application logs in Render dashboard
- Verify the JAR file is being created correctly
- Ensure all required environment variables are set

## 📞 Support

- Render Documentation: https://render.com/docs
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Application Logs: Available in Render dashboard 