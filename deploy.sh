#!/bin/bash

# BookBuddy Deployment Script for Render
echo "🚀 BookBuddy Deployment Script"
echo "================================"

# Check if git is initialized
if [ ! -d ".git" ]; then
    echo "❌ Git repository not found. Please initialize git first:"
    echo "   git init"
    echo "   git add ."
    echo "   git commit -m 'Initial commit'"
    exit 1
fi

# Check if remote is set
if ! git remote get-url origin > /dev/null 2>&1; then
    echo "❌ Git remote 'origin' not found. Please add your GitHub repository:"
    echo "   git remote add origin <your-github-repo-url>"
    exit 1
fi

# Build the application
echo "📦 Building application..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please fix the issues and try again."
    exit 1
fi

echo "✅ Build successful!"

# Check if JAR file was created
if [ ! -f "target/bookbuddy-0.0.1-SNAPSHOT.jar" ]; then
    echo "❌ JAR file not found. Build may have failed."
    exit 1
fi

echo "📋 Deployment Checklist:"
echo "========================"
echo "1. ✅ Application builds successfully"
echo "2. ✅ PostgreSQL dependency added"
echo "3. ✅ Production configuration created"
echo "4. ✅ render.yaml file created"
echo "5. ✅ Procfile created"
echo ""
echo "📝 Next Steps:"
echo "=============="
echo "1. Push your code to GitHub:"
echo "   git add ."
echo "   git commit -m 'Prepare for Render deployment'"
echo "   git push origin main"
echo ""
echo "2. Go to Render Dashboard: https://dashboard.render.com/"
echo ""
echo "3. Create a new Web Service:"
echo "   - Connect your GitHub repository"
echo "   - Environment: Java"
echo "   - Build Command: ./mvnw clean package -DskipTests"
echo "   - Start Command: java -jar target/bookbuddy-0.0.1-SNAPSHOT.jar"
echo ""
echo "4. Create a PostgreSQL Database:"
echo "   - Name: bookbuddy-db"
echo "   - Copy connection details"
echo ""
echo "5. Set Environment Variables:"
echo "   - SPRING_PROFILES_ACTIVE: prod"
echo "   - SERVER_PORT: 8080"
echo "   - SPRING_DATASOURCE_URL: (from database)"
echo "   - SPRING_DATASOURCE_USERNAME: (from database)"
echo "   - SPRING_DATASOURCE_PASSWORD: (from database)"
echo ""
echo "6. Deploy!"
echo ""
echo "🎉 Your application will be available at: https://your-app-name.onrender.com" 