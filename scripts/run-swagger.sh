#!/bin/bash

# Leafresh API Documentation Generator
# This script starts the application in Swagger mode for API documentation

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_PORT=${SWAGGER_PORT:-8080}
TIMEOUT=${SWAGGER_TIMEOUT:-120}
LOG_FILE="swagger-app.log"

echo -e "${BLUE}🌱 Leafresh API Documentation Generator${NC}"
echo "================================================"

# Check if port is available
if lsof -i :$APP_PORT >/dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Port $APP_PORT is already in use${NC}"
    echo "Please stop the running service or use a different port:"
    echo "  SWAGGER_PORT=8081 $0"
    exit 1
fi

# Clean up previous log file
rm -f $LOG_FILE

echo -e "${BLUE}🚀 Starting application with Swagger profile...${NC}"

# Start application in background
./gradlew runSwagger > $LOG_FILE 2>&1 &
APP_PID=$!
echo $APP_PID > swagger.pid

echo -e "${YELLOW}📋 Application PID: $APP_PID${NC}"

# Function to cleanup on exit
cleanup() {
    echo -e "\n${YELLOW}🛑 Cleaning up...${NC}"
    if [ -f swagger.pid ]; then
        PID=$(cat swagger.pid)
        if kill -0 $PID 2>/dev/null; then
            echo -e "${YELLOW}🛑 Stopping application (PID: $PID)${NC}"
            kill $PID 2>/dev/null || true
            sleep 2
            # Force kill if still running
            kill -9 $PID 2>/dev/null || true
        fi
        rm -f swagger.pid
    fi
}

# Set trap to cleanup on script exit
trap cleanup EXIT INT TERM

# Wait for application to start
echo -e "${YELLOW}⏳ Waiting for application to start (timeout: ${TIMEOUT}s)...${NC}"

count=0
while [ $count -lt $TIMEOUT ]; do
    if curl -s http://localhost:$APP_PORT/actuator/health >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Application is ready!${NC}"
        break
    fi
    
    # Check if process is still running
    if ! kill -0 $APP_PID 2>/dev/null; then
        echo -e "${RED}❌ Application process died. Check logs:${NC}"
        tail -20 $LOG_FILE
        exit 1
    fi
    
    if [ $count -eq $(($TIMEOUT - 1)) ]; then
        echo -e "${RED}❌ Application failed to start within $TIMEOUT seconds${NC}"
        echo -e "${RED}📋 Last 20 lines of log:${NC}"
        tail -20 $LOG_FILE
        exit 1
    fi
    
    # Show progress dots
    if [ $((count % 5)) -eq 0 ]; then
        echo -e "${YELLOW}⏱️  Waiting... ($((count + 1))/$TIMEOUT)${NC}"
    fi
    
    sleep 1
    count=$((count + 1))
done

echo ""
echo -e "${GREEN}🎉 Leafresh API Documentation is now running!${NC}"
echo "================================================"
echo -e "${GREEN}📖 Swagger UI:${NC}       http://localhost:$APP_PORT/swagger-ui.html"
echo -e "${GREEN}📄 OpenAPI JSON:${NC}     http://localhost:$APP_PORT/v3/api-docs"
echo -e "${GREEN}🏥 Health Check:${NC}     http://localhost:$APP_PORT/actuator/health"
echo -e "${GREEN}🗄️  H2 Console:${NC}      http://localhost:$APP_PORT/h2-console"
echo ""
echo -e "${BLUE}💡 Tips:${NC}"
echo "  • Press Ctrl+C to stop the documentation server"
echo "  • Check logs: tail -f $LOG_FILE"
echo "  • Application PID: $APP_PID"
echo ""

# Optional: Open browser automatically
if command -v open >/dev/null 2>&1; then
    echo -e "${BLUE}🌐 Opening Swagger UI in browser...${NC}"
    open "http://localhost:$APP_PORT/swagger-ui.html"
elif command -v xdg-open >/dev/null 2>&1; then
    echo -e "${BLUE}🌐 Opening Swagger UI in browser...${NC}"
    xdg-open "http://localhost:$APP_PORT/swagger-ui.html"
fi

# Keep script running until user stops it
echo -e "${YELLOW}🔄 Documentation server is running. Press Ctrl+C to stop.${NC}"

# Wait for the application process to end or user interruption
wait $APP_PID 2>/dev/null || true

echo -e "${GREEN}👋 Documentation server stopped.${NC}"
