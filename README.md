## Hi there 👋


## Siemens Java Internship - Code Refactoring Project

This repository contains a Spring Boot application that implements a simple CRUD system with some asynchronous processing capabilities. The application was created by a development team in a hurry and while it implements all required features, the code quality needs significant improvement.

## Getting Started
- Clone this repository
- Import the project into your IDE as a Maven project (Java 17, might work with other Java versions as well)
- Study the existing code and identify issues
- Implement your refactoring changes
- Test thoroughly to ensure functionality is preserved

## Your Assignment
  The Project should follow the following structure:
Project Structure
├── src/
│   ├── main/
│   │   ├── java/com/siemens/internship/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── Application.java
│   │   └── resources/
│   └── test/
├── pom.xml
└── README.md
ⓘ
##  You will have to:
1. Fix all logical errors while maintaining the same functionality
2. Implement proper error handling and validation
3. Be well-documented with clear, concise comments
4. Write test functions with as much coverage as possible
5. Make sure that the Status Codes used in Controller are correct
6. Find a way to implement an email validation
7. Refactor the **processItemsAsync** function
    The **processItemsAsync** function is supposed to:
      1. Asynchronously process EVERY item (retrieve from database, update status, and save)
      2. Track which items were processed
      3. Return a list when all items have been processed
      4. Provide an accurate list of all successfully processed items
      HINT: You are free to modify the function and variables as much as you want :)


You can zip your solution and send it back to us!

