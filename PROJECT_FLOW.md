# Document Management System: A Simple API Flow Guide

## 1. Introduction

This document provides a functional overview of the Document Management System. Its purpose is to detail the end-to-end workflow of each primary API, illustrating how user requests are processed throughout the application stack. It serves as a practical guide for developers to understand the system's operational logic.

## 2. The Big Picture: What Happens When?

Our application has a few key jobs:
1.  Letting users sign up and log in.
2.  Allowing users to upload documents.
3.  Making those documents searchable.
4.  Allowing users to retrieve lists of documents.

We will walk through each of these flows step-by-step.

---

### **Flow 1: Becoming a User (Registration & Login)**

This is the entry point for any user. They need an account to do anything meaningful.

**APIs:** `POST /api/auth/register`, `POST /api/auth/login`

**The Story:**

1.  **Registration:** A new user fills out a form with their username, password, and desired role (e.g., "EDITOR"). When they submit it, a request hits our `/register` API.
    -   To keep the password safe, we don't store it as plain text. We use a security function to scramble it into a unique, irreversible hash.
    -   We then save the new user, with their scrambled password, into our main **H2 database**.

2.  **Login:** The user now tries to log in using the `/login` API.
    -   Our application takes the password they just provided, scrambles it using the *exact same method*, and compares it to the scrambled password stored in the database.
    -   If they match, we know the user is who they say they are.
    -   We then generate a special, temporary access pass called a **JWT Token**. This token is like a ticket that proves the user is logged in. They will need to show this ticket for every other API call they make.

---

### **Flow 2: Uploading a Document (The Main Event)**

This is the most important and complex flow in our system. We've designed it to be extremely fast for the user.

**API:** `POST /api/documents/upload`

**The Story:**

1.  **The Upload:** A logged-in user with "EDITOR" or "ADMIN" rights uploads a document (e.g., a PDF). Their request, carrying their JWT token (the "ticket") and the file, hits our `/upload` API.

2.  **The Immediate Response:** Our API does something very clever here. It **does not** open the file, read its content, or try to process it. Doing so would make the user wait. Instead, it does two simple things:
    -   It takes the file and its details and wraps them up into a "to-do" message.
    -   It places this message into a digital mailbox system called **RabbitMQ**.
    -   It then immediately tells the user, "Got it, I'll take care of this!" by sending a `202 Accepted` response. From the user's perspective, the upload was instant.

3.  **The Background Worker:**
    -   A completely separate part of our application, the `DocumentQueueListener`, acts as a "worker" whose only job is to check the **RabbitMQ** mailbox for new messages.
    -   As soon as our message appears, this worker picks it up. Now the real work begins, completely hidden from the user.
    -   The worker opens the file and uses a tool called **Apache Tika** to read and extract all the text from inside it.
    -   It saves the document's basic information (like its title and author) into our main **H2 database**.
    -   Finally, it sends all the extracted text to our powerful search engine, **Elasticsearch**. Think of Elasticsearch as a super-librarian for text. It reads all the content and creates a highly optimized index, so it knows where every single word is located across all our documents.

---

### **Flow 3: Finding Documents (The Two-Speed Approach)**

Users often need to see a list of the documents they've uploaded. We make this fast by using a cache.

**API:** `GET /api/documents`

**The Story:**

1.  **The Request:** A user asks for a list of documents, maybe with a filter like "show me all PDFs."

2.  **The Fast Lane (Checking the Cache):**
    -   The API first checks a high-speed storage spot called **Redis**. Redis is our cache—think of it as a small desk where we keep documents we need to access often.
    -   If the exact list the user asked for is already in Redis, the API grabs it from there and sends it back instantly. This is the fastest possible outcome.

3.  **The Slower Lane (The First Time):**
    -   If the list is *not* in Redis (perhaps this is the first time anyone has asked for this specific list), the API goes to our main **H2 database** to fetch the information.
    -   But before sending the list to the user, it does one extra step: it saves a copy of the list in our **Redis** cache.
    -   This way, the *next time* anyone asks for this same list, it will be waiting in the "fast lane."

---

### **Flow 4: Searching Inside Documents (The Power Search)**

This is where we see the real power of our architecture.

**API:** `GET /api/qa/search`

**The Story:**

1.  **The Search Query:** A user wants to find every document that mentions the word "invoice." They use the `/search` API.

2.  **The Smart Search Engine:**
    -   Our API knows that trying to read every document in our main H2 database to find a word would be incredibly slow.
    -   So, it doesn't even look there. Instead, it sends the query directly to our "super-librarian," **Elasticsearch**.
    -   Because Elasticsearch has already read and indexed all the content during the upload process (in Flow 2), it can find all occurrences of "invoice" almost instantly.
    -   Elasticsearch replies with a list of all the documents that contain the word.

3.  **The Final Result:** The API returns this list of matching documents to the user, providing a fast and powerful search experience.

## 4. Project Structure Overview

```
document-management/
├── src/
│   ├── main/
│   │   ├── java/com/example/docmgmt/
│   │   │   ├── config/          # Spring Security, Redis, RabbitMQ, etc. configuration.
│   │   │   ├── controller/      # REST API endpoints (web layer).
│   │   │   ├── model/           # Data models (JPA Entities, DTOs, etc.).
│   │   │   ├── repository/      # Spring Data repositories for DB & Elasticsearch.
│   │   │   ├── service/         # Business logic layer.
│   │   │   ├── messaging/       # RabbitMQ configuration.
│   │   │   └── messaginglistner/# RabbitMQ message listeners.
│   │   ├── resources/
│   │   │   └── application.properties # Main application configuration.
├── docker-compose.yml           # Defines and configures all services.
├── Dockerfile                   # Instructions to build the application's Docker image.
├── pom.xml                      # Maven project dependencies and build configuration.
└── README.md                    # High-level project overview and setup guide.
``` 