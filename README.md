# Clicker System

## Project Overview

Clicker System is a simple client-server application developed as a student project. The purpose of the project is to demonstrate how an Android front end can communicate with a Java servlet backend and a MySQL database.

The system allows users to:

- register a new account
- log in with an existing account
- answer teacher-created four-option questions
- post comments on a specific question
- view discussion for the currently selected question
- view voting results through a backend display page

Teachers can create questions from a backend web page, and the Android app loads the active questions dynamically.

## Project Objectives

The main objectives of this project are:

- to build an Android application with multiple screens
- to connect the Android app to a backend server using HTTP requests
- to store and retrieve data using MySQL
- to implement user registration and login
- to demonstrate basic full-stack integration between mobile app, server, and database

## System Components

This project consists of two main parts:

### 1. Android Application

The Android app is built in Java and contains the following activities:

- `LoginActivity`
- `RegisterActivity`
- `MainActivity`

Functions of the Android app:

- user login
- user registration
- dynamic question loading from the backend
- choice submission for the selected question
- comment submission tied to the selected question
- comment display tied to the selected question
- simple question navigation with previous, next, and refresh controls

### 2. Java Servlet Backend

The backend is built using Java Servlets and handles requests from the Android app.

Main backend components:

- `LoginServlet.java`
- `RegisterServlet.java`
- `SelectServlet.java`
- `DisplayServlet.java`
- `AddCommentServlet.java`
- `GetCommentsServlet.java`
- `QuestionsServlet.java`
- `TeacherDashboardServlet.java`
- `CreateQuestionServlet.java`

Functions of the backend:

- validate login credentials
- register new users
- let teachers create new four-option questions
- return active questions to the Android app
- save voting responses
- save comments for a specific question
- retrieve comments for a specific question
- display vote totals in HTML format
- hash passwords before storing them

### 3. MySQL Database

The MySQL database stores the application data in the following tables:

- `users`
- `questions`
- `responses`
- `comments`

The SQL setup file is located at:

`backend-src/setup.sql`

## Project Structure

- `app/`
  Contains the Android Studio project and source code for the mobile app
- `backend-src/`
  Contains Java servlet source files, web configuration, database setup script, and supporting backend files
- `backend-src/setup.sql`
  Contains SQL statements to create the database and tables

## Technologies Used

- Java
- Android Studio
- Gradle
- Java Servlets
- MySQL
- JDBC

## How the System Works

1. A user opens the Android application.
2. The user can register a new account or log in with an existing account.
3. After login, the user enters the main page.
4. The Android app loads active questions created by the teacher from the backend.
5. The user selects one answer from the available options for the current question.
6. The backend stores the response in the MySQL database.
7. The user can also submit comments for the current question.
8. The backend stores comments with the matching `question_id` and returns only the discussion for that question.
9. User passwords are hashed before storage in the database.
10. Voting results can be viewed through the servlet results page.

Teachers can open the backend dashboard to create new questions at:

`http://localhost:9999/clicker/teacher`

## Backend Endpoints

The backend provides the following endpoints:

- `/register`
- `/login`
- `/questions`
- `/select`
- `/display`
- `/teacher`
- `/teacher/create`
- `/AddCommentServlet`
- `/GetCommentsServlet`

The Android emulator is currently configured to access the backend using:

`http://10.0.2.2:9999/clicker/`

This means the backend is expected to run locally on:

`http://localhost:9999/clicker/`

## Database Setup

Run the SQL file below in MySQL to create the required database and tables:

```sql
SOURCE backend-src/setup.sql;
```

This creates:

- database: `clicker`
- table: `users`
- table: `questions`
- table: `responses`
- table: `comments`

## Default Database Configuration

The servlet files currently use these database settings:

- database name: `clicker`
- username: `myuser`
- password: `12345678`

These values are hardcoded in the backend source code.

## How to Run the Project

### 1. Set up MySQL

- start the MySQL server
- create the database and tables using `backend-src/setup.sql`
- make sure the configured MySQL user has access

### 2. Deploy the backend

- deploy the servlet project to a Java servlet container such as Tomcat
- make sure it runs on port `9999`
- make sure the context path is `/clicker`

In the current local setup, Tomcat's `webapps/clicker` points to:

`/Users/goodwilllion/IM2073/Clicker-system/backend-src`

Compile the backend servlets into `WEB-INF/classes` with:

```bash
cd /Users/goodwilllion/IM2073/Clicker-system/backend-src/WEB-INF/classes
javac -cp "/Users/goodwilllion/Web-Programming/tomcat/lib/servlet-api.jar:../lib/mysql-connector-j-9.6.0.jar" -d . ../../*.java
```

Start Tomcat with:

```bash
/Users/goodwilllion/Web-Programming/tomcat/bin/startup.sh
```

Stop Tomcat with:

```bash
/Users/goodwilllion/Web-Programming/tomcat/bin/shutdown.sh
```

If the backend address is changed, update the URLs in:

- `app/src/main/java/com/example/clickerapp/LoginActivity.java`
- `app/src/main/java/com/example/clickerapp/RegisterActivity.java`
- `app/src/main/java/com/example/clickerapp/MainActivity.java`

### 3. Run the Android app

- open the project in Android Studio
- run the app on an Android emulator or device

If a physical device is used, replace `10.0.2.2` with the IP address of the computer hosting the backend.

## Output

The application produces two main outputs:

- Android app interface for login, dynamic question loading, voting, and per-question discussion
- backend teacher dashboard for creating questions
- HTML results page showing vote totals for teacher-created questions

Example results page:

`http://localhost:9999/clicker/display`

## Limitations

- login and registration use query parameters over HTTP
- database configuration is hardcoded
- teacher question management currently uses a simple backend web page without authentication
- backend deployment steps are not automated inside this repository

## Possible Improvements

- use `POST` requests for login and registration
- move database configuration into a properties file or environment variables
- add teacher authentication and question editing
- add one-vote-per-user validation per question
- improve the user interface design
- separate compiled backend files from source files

## Conclusion

This project demonstrates the basic development of a full-stack mobile voting system using Android, Java Servlets, and MySQL. It shows how a mobile application can interact with a backend server to perform user authentication, submit voting data, and display shared comments and results.
