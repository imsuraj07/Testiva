import { useState, useEffect, useCallback } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { authAPI } from './services/api';

import PublicLayout from './layouts/PublicLayout';
import HomePage from './pages/public/HomePage';
import AboutUsPage from './pages/public/AboutUsPage';
import ContactUsPage from './pages/public/ContactUsPage';
import RegistrationPage from './pages/public/RegistrationPage';

import LoginPage from './pages/LoginPage';
import AdminLayout from './layouts/AdminLayout';
import StudentLayout from './layouts/StudentLayout';

import AdminDashboard from './pages/admin/AdminDashboard';
import ManageStudents from './pages/admin/ManageStudents';
import ScheduleTest from './pages/admin/ScheduleTest';
import QuestionBank from './pages/admin/QuestionBank';
import ManageResults from './pages/admin/ManageResults';
import ManageEnquiries from './pages/admin/ManageEnquiries';

import StudentDashboard from './pages/student/StudentDashboard';
import GiveTest from './pages/student/GiveTest';
import TestInterface from './pages/student/TestInterface';
import StudentResults from './pages/student/StudentResults';

function App() {
  const [user, setUser] = useState(null);
  const [checking, setChecking] = useState(true);

  // Check existing session on mount
  useEffect(() => {
    authAPI.me().then(res => {
      setUser(res.data);
      setChecking(false);
    }).catch(() => {
      setUser(null);
      setChecking(false);
    });
  }, []);

  // Login handler — stores user in state
  const handleLogin = useCallback((userData) => {
    setUser(userData);
  }, []);

  // Logout handler — clears user state (this is the critical fix)
  const handleLogout = useCallback(async () => {
    try {
      await authAPI.logout();
    } catch {
      // Even if API fails, clear local state
    }
    setUser(null); // ← This clears the guard so /login actually renders
  }, []);

  if (checking) {
    return (
      <div className="min-h-screen bg-bg flex items-center justify-center">
        <div className="text-center">
          <div className="spinner mx-auto mb-4" />
          <p className="text-text-secondary">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <BrowserRouter>
      <Toaster
        position="top-right"
        toastOptions={{
          style: {
            background: '#1e1b4b',
            color: '#f1f5f9',
            border: '1px solid rgba(255,255,255,0.1)',
            borderRadius: '12px',
            fontSize: '14px',
          },
          success: { iconTheme: { primary: '#10b981', secondary: '#fff' } },
          error: { iconTheme: { primary: '#ef4444', secondary: '#fff' } },
        }}
      />

      <Routes>
        {/* Public Routes with Navbar and Footer */}
        <Route element={<PublicLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/about" element={<AboutUsPage />} />
          <Route path="/contact" element={<ContactUsPage />} />
          <Route path="/register" element={
            user ? <Navigate to={user.role === 'ADMIN' ? '/admin/dashboard' : '/student/dashboard'} /> :
            <RegistrationPage />
          } />
          <Route path="/login" element={
            user ? <Navigate to={user.role === 'ADMIN' ? '/admin/dashboard' : '/student/dashboard'} /> :
            <LoginPage onLogin={handleLogin} />
          } />
        </Route>

        {/* Admin Routes */}
        <Route path="/admin/*" element={
          !user || user.role !== 'ADMIN' ? <Navigate to="/login" /> :
          <AdminLayout user={user} onLogout={handleLogout}>
            <Routes>
              <Route path="dashboard" element={<AdminDashboard />} />
              <Route path="students" element={<ManageStudents />} />
              <Route path="schedule-test" element={<ScheduleTest />} />
              <Route path="questions" element={<QuestionBank />} />
              <Route path="results" element={<ManageResults />} />
              <Route path="enquiries" element={<ManageEnquiries />} />
              <Route path="*" element={<Navigate to="dashboard" />} />
            </Routes>
          </AdminLayout>
        } />

        {/* Student Test Interface (full screen, no sidebar) */}
        <Route path="/student/test/:id" element={
          !user || user.role !== 'STUDENT' ? <Navigate to="/login" /> :
          <TestInterface />
        } />

        {/* Student Routes */}
        <Route path="/student/*" element={
          !user || user.role !== 'STUDENT' ? <Navigate to="/login" /> :
          <StudentLayout user={user} onLogout={handleLogout}>
            <Routes>
              <Route path="dashboard" element={<StudentDashboard />} />
              <Route path="give-test" element={<GiveTest />} />
              <Route path="results" element={<StudentResults />} />
              <Route path="*" element={<Navigate to="dashboard" />} />
            </Routes>
          </StudentLayout>
        } />

        {/* Default */}
        <Route path="*" element={<Navigate to={user ? (user.role === 'ADMIN' ? '/admin/dashboard' : '/student/dashboard') : '/login'} />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
