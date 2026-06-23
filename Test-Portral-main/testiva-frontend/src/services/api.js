import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

// Global response interceptor — handle session expiry
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // If any API returns 401 (session expired) and we're not on the login/auth endpoints,
    // redirect to login page
    if (error.response?.status === 401 && !error.config?.url?.includes('/auth/')) {
      // Clear any stale state and redirect
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// =============== AUTH & PUBLIC ===============
export const publicAPI = {
  register: (formData) => api.post('/public/register', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  contact: (data) => api.post('/public/contact', data),
};

export const authAPI = {
  adminLogin: (email, password) => api.post('/auth/admin/login', { email, password }),
  studentLogin: (email, password) => api.post('/auth/student/login', { email, password }),
  logout: () => api.post('/auth/logout'),
  me: () => api.get('/auth/me'),
};

// =============== ADMIN ===============
export const adminAPI = {
  getDashboard: () => api.get('/admin/dashboard'),
  getStudents: () => api.get('/admin/students'),
  toggleStudentStatus: (id) => api.put(`/admin/students/${id}/status`),
  getTests: () => api.get('/admin/tests'),
  scheduleTest: (data) => api.post('/admin/tests', data),
  updateTest: (id, data) => api.put(`/admin/tests/${id}`, data),
  deleteTest: (id) => api.delete(`/admin/tests/${id}`),
  getQuestions: () => api.get('/admin/questions'),
  addQuestion: (data) => api.post('/admin/questions', data),
  uploadQuestions: (formData) => api.post('/admin/questions/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  deleteQuestion: (id) => api.delete(`/admin/questions/${id}`),
  assignQuestions: (testId, questionIds) => api.post('/admin/questions/assign', { testId, questionIds }),
  getResults: () => api.get('/admin/results'),
  getEnquiries: () => api.get('/admin/enquiries'),
};

// =============== STUDENT ===============
export const studentAPI = {
  getDashboard: () => api.get('/student/dashboard'),
  getTests: () => api.get('/student/tests'),
  validateTest: (testId) => api.post('/student/validate-test', { testId }),
  startTest: (id) => api.get(`/student/start-test/${id}`),
  submitTest: (testId, answers) => api.post('/student/submit-test', { testId, answers }),
  getResults: () => api.get('/student/results'),
};

export default api;
