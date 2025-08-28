#!/bin/bash

# Quick test script for Swagger documentation generation
# This script tests if the Swagger endpoints are working correctly

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"
TIMEOUT=5

echo -e "${BLUE}🧪 Testing Leafresh Swagger Endpoints${NC}"
echo "========================================"

# Function to test an endpoint
test_endpoint() {
    local url=$1
    local name=$2
    
    echo -n "Testing $name... "
    
    if curl -s --max-time $TIMEOUT "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ OK${NC}"
        return 0
    else
        echo -e "${RED}❌ FAIL${NC}"
        return 1
    fi
}

# Function to test JSON endpoint and validate content
test_json_endpoint() {
    local url=$1
    local name=$2
    
    echo -n "Testing $name... "
    
    local response=$(curl -s --max-time $TIMEOUT "$url" 2>/dev/null)
    
    if [ -z "$response" ]; then
        echo -e "${RED}❌ No response${NC}"
        return 1
    fi
    
    # Check if response is valid JSON and contains OpenAPI spec
    if echo "$response" | jq -e '.openapi' > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Valid OpenAPI spec${NC}"
        
        # Extract basic info
        local title=$(echo "$response" | jq -r '.info.title // "N/A"')
        local version=$(echo "$response" | jq -r '.info.version // "N/A"')
        echo -e "  ${BLUE}📖 Title:${NC} $title"
        echo -e "  ${BLUE}🔖 Version:${NC} $version"
        
        return 0
    else
        echo -e "${RED}❌ Invalid OpenAPI format${NC}"
        return 1
    fi
}

# Check if jq is available
if ! command -v jq >/dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  jq not found. JSON validation will be skipped.${NC}"
    USE_JQ=false
else
    USE_JQ=true
fi

echo -e "${YELLOW}🔍 Testing application health...${NC}"

# Test application health
if ! test_endpoint "$BASE_URL/actuator/health" "Health Check"; then
    echo -e "${RED}❌ Application is not running or not healthy${NC}"
    echo -e "${YELLOW}💡 Start the application with: ./scripts/run-swagger.sh${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}🔍 Testing Swagger endpoints...${NC}"

# Test Swagger UI
test_endpoint "$BASE_URL/swagger-ui.html" "Swagger UI"
test_endpoint "$BASE_URL/swagger-ui/index.html" "Swagger UI (alternative)"

# Test OpenAPI JSON
if [ "$USE_JQ" = true ]; then
    test_json_endpoint "$BASE_URL/v3/api-docs" "OpenAPI JSON Spec"
else
    test_endpoint "$BASE_URL/v3/api-docs" "OpenAPI JSON Spec"
fi

echo ""
echo -e "${YELLOW}🔍 Testing additional endpoints...${NC}"

# Test H2 console (if available)
test_endpoint "$BASE_URL/h2-console" "H2 Database Console"

# Test management endpoints
test_endpoint "$BASE_URL/actuator" "Actuator Root"
test_endpoint "$BASE_URL/actuator/info" "Application Info"

echo ""
echo -e "${GREEN}🎉 Testing completed!${NC}"
echo ""
echo -e "${BLUE}📖 Access URLs:${NC}"
echo "  Swagger UI:     $BASE_URL/swagger-ui.html"
echo "  OpenAPI JSON:   $BASE_URL/v3/api-docs"
echo "  H2 Console:     $BASE_URL/h2-console"
echo "  Health Check:   $BASE_URL/actuator/health"
echo ""

# Optional: Show OpenAPI spec summary
if [ "$USE_JQ" = true ]; then
    echo -e "${BLUE}📋 API Summary:${NC}"
    local spec=$(curl -s --max-time $TIMEOUT "$BASE_URL/v3/api-docs" 2>/dev/null)
    if [ ! -z "$spec" ]; then
        local paths_count=$(echo "$spec" | jq '.paths | length' 2>/dev/null || echo "N/A")
        local components_count=$(echo "$spec" | jq '.components.schemas | length' 2>/dev/null || echo "N/A")
        echo "  Total API endpoints: $paths_count"
        echo "  Schema components:   $components_count"
    fi
fi
