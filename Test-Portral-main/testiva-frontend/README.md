<div align="center">
  <h1>🎓 Testiva Frontend</h1>
  <p><strong>The modern, responsive user interface for the Testiva Portal.</strong></p>
  
  [![React](https://img.shields.io/badge/React-19.2-blue.svg?logo=react)](https://react.dev/)
  [![Vite](https://img.shields.io/badge/Vite-8.0-646CFF.svg?logo=vite)](https://vitejs.dev/)
  [![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-4.2-06B6D4.svg?logo=tailwindcss)](https://tailwindcss.com/)
</div>

<hr/>

## 📖 Overview

This directory contains the frontend client for **Testiva**, a comprehensive web-based examination and study management system. Built with modern web technologies, this Single Page Application (SPA) ensures a fast, responsive, and intuitive user experience.

---

## 🛠️ Technology Stack

- **Framework**: [React](https://react.dev/)
- **Build Tool**: [Vite](https://vitejs.dev/)
- **Styling**: [Tailwind CSS](https://tailwindcss.com/)
- **Routing**: [React Router](https://reactrouter.com/)
- **HTTP Client**: [Axios](https://axios-http.com/)
- **Icons**: [React Icons](https://react-icons.github.io/react-icons/)
- **Notifications**: [React Hot Toast](https://react-hot-toast.com/)

---

## ⚙️ Quick Start

### Prerequisites
- [Node.js](https://nodejs.org/) (v18 or higher recommended)
- `npm`

### Installation
1. Ensure you are in the frontend directory:
   ```bash
   cd testiva-frontend
   ```
2. Install the required dependencies:
   ```bash
   npm install
   ```

### Development Server
To spin up the local development server:
```bash
npm run dev
```
Open your browser and visit the URL displayed in your terminal (typically `http://localhost:5173`). The server features Hot Module Replacement (HMR) for instant feedback during development.

### Building for Production
To generate a highly optimized production build:
```bash
npm run build
```
The compiled assets will be available in the `dist` folder.

---

## 🔗 Integration with Backend

This frontend is designed to interact with the **Testiva Spring Boot API**. Ensure the backend server is running (usually on `http://localhost:9090`) to utilize all features like user authentication, fetching test results, and displaying dynamic content.

Configure the API base URL in your environment variables or directly in your Axios configuration setup.
