<div align="center">
  <h1>🎓 Testiva</h1>
  <p><strong>A Modern, Comprehensive Online Examination & Study Management Portal</strong></p>
  
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
  [![Java 21](https://img.shields.io/badge/Java-21-orange.svg?logo=java)](https://oracle.com/java)
  [![React](https://img.shields.io/badge/React-19.2-blue.svg?logo=react)](https://react.dev/)
  [![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-4.2-06B6D4.svg?logo=tailwindcss)](https://tailwindcss.com/)
  [![MySQL](https://img.shields.io/badge/MySQL-Database-blue.svg?logo=mysql)](https://www.mysql.com/)
</div>

<hr/>

## 📖 Overview

**Testiva** is an advanced, web-based examination and study management system engineered to revolutionize how educational institutions conduct tests and distribute resources. Transitioning from legacy templates to a high-performance **React frontend** powered by a robust **Spring Boot REST API**, Testiva delivers an intuitive, SaaS-style experience for both students and administrators.

---

## 🚀 Key Features

### 👨‍🎓 Student Portal
- **Secure Onboarding**: Seamless registration and dynamic profile management with picture uploads.
- **Live Examinations**: Browse active schedules and attempt tests within a secure, controlled digital environment.
- **Instantaneous Analytics**: Real-time evaluation and scoring immediately after submission.
- **Performance Archive**: Comprehensive historical performance tracking and detailed scorecards.
- **Resource Hub**: Streamlined access to shared PDF materials and embedded educational video tutorials.
- **Direct Support**: Integrated ticketing/enquiry system to seamlessly communicate with administrators.

### 🛡️ Administrator Console
- **Centralized Dashboard**: Real-time statistical overview of the entire ecosystem (active students, pending tests, uploaded materials).
- **Assessment Management**: Intuitive scheduling of new tests and full CRUD operations on the dynamic Question Bank.
- **User Governance**: End-to-end monitoring and management of registered student accounts.
- **Content Curation**: Organize, upload, and distribute PDF study guides and video resources.
- **Advanced Analytics**: Exportable, granular insights into student test results and overall performance.
- **Helpdesk**: Centralized communication hub to resolve student enquiries effectively.

---

## 🛠️ Technology Stack

Testiva leverages a modern, decoupled architecture ensuring scalability, performance, and a premium user experience.

### Backend (RESTful API)
- **Framework**: Spring Boot (v3.5.3)
- **Language**: Java 21
- **Database**: MySQL
- **ORM/Data Access**: Spring Data JPA & Hibernate
- **Mailing**: JavaMailSender (Gmail SMTP Integration)
- **Build Tool**: Maven

### Frontend (SPA)
- **Framework**: React 19 (via Vite)
- **Styling**: Tailwind CSS 4.2
- **Routing**: React Router DOM
- **HTTP Client**: Axios
- **Notifications**: React Hot Toast
- **Icons**: React Icons

*(Note: The system is currently migrating from legacy Thymeleaf/Bootstrap templates to this decoupled React architecture.)*

---

## 📁 Project Structure

```text
Testiva/
├── testiva-frontend/        # React SPA Frontend (Vite)
│   ├── src/                 # React components, pages, and context
│   ├── public/              # Static frontend assets
│   ├── package.json         # Node dependencies
│   └── vite.config.js       # Vite configuration
│
└── src/                     # Spring Boot Backend
    ├── main/java/.../testiva/
    │   ├── Controller/      # REST API Endpoints
    │   ├── Model/           # JPA Entities (StudentInfo, TestInfo, etc.)
    │   ├── Repository/      # Data access layer (JpaRepository)
    │   ├── ServiceAPI/      # Core business logic
    │   └── TestivaApplication.java
    └── main/resources/
        ├── application.properties # Environment configuration
        └── templates/       # Legacy Thymeleaf views
```

---

## ⚙️ Setup & Installation

### Prerequisites
- **Java**: JDK 21+
- **Database**: MySQL Server
- **Node.js**: v18+ (for frontend)
- **Maven**: Latest version

### 1. Database Configuration
1. Create the MySQL database:
   ```sql
   CREATE DATABASE test_portal;
   ```
2. Update `src/main/resources/application.properties` with your credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/test_portal
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD
   ```

### 2. Email Configuration
The system uses Gmail SMTP for notifications.
1. Generate a **Google App Password**.
2. Update `application.properties`:
   ```properties
   spring.mail.username=YOUR_GMAIL
   spring.mail.password=YOUR_APP_PASSWORD
   ```

### 3. Launching the Backend
Open your terminal in the project root:
```bash
mvn spring-boot:run
```
*The API will start running on `http://localhost:9090`.*

### 4. Launching the Frontend
Open a new terminal, navigate to the frontend directory:
```bash
cd testiva-frontend
npm install
npm run dev
```
*The modern React UI will be accessible via the Vite dev server URL (typically `http://localhost:5173`).*

---

## 🌟 Future Roadmap
- [ ] Complete migration of all administrator dashboards to React.
- [ ] Implement robust JWT-based authentication for the REST API.
- [ ] Add advanced anti-cheating mechanisms for live exams.
- [ ] Integrate payment gateways for premium courses.

---

## 📧 Contact & Support
For any queries, feedback, or contribution requests, please reach out via the "Contact Us" section within the application portal.

<div align="center">
  <i>Empowering digital education, one test at a time.</i>
</div>
