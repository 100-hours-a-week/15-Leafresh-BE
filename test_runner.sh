#!/bin/bash
cd backend
java -cp $(./gradlew -q printClasspath) ktb.leafresh.backend.global.security.SimpleBloomFilterPerformanceTest
