import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { HiOutlineViewGrid, HiOutlineUsers, HiOutlineClipboardList, HiOutlineCalendar, HiOutlineChartBar, HiOutlineQuestionMarkCircle, HiOutlineLogout, HiOutlineMenu, HiOutlineX } from 'react-icons/hi';
import toast from 'react-hot-toast';

const adminLinks = [
  { path: '/admin/dashboard', label: 'Dashboard', icon: HiOutlineViewGrid },
  { path: '/admin/students', label: 'Students', icon: HiOutlineUsers },
  { path: '/admin/schedule-test', label: 'Schedule Test', icon: HiOutlineCalendar },
  { path: '/admin/questions', label: 'Question Bank', icon: HiOutlineQuestionMarkCircle },
  { path: '/admin/results', label: 'Results', icon: HiOutlineChartBar },
  { path: '/admin/enquiries', label: 'Enquiries', icon: HiOutlineClipboardList },
];

export default function AdminLayout({ children, user, onLogout }) {
  const location = useLocation();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = async () => {
    await onLogout();           // Clears user state in App.jsx + calls API
    toast.success('Logged out');
    navigate('/login');          // Now /login renders correctly since user is null
  };

  return (
    <div className="flex h-screen overflow-hidden">
      {/* Sidebar */}
      <aside className={`fixed inset-y-0 left-0 z-40 w-64 bg-surface border-r border-border transform transition-transform duration-300 lg:translate-x-0 lg:relative ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <div className="flex flex-col h-full">
          {/* Logo */}
          <div className="flex items-center gap-3 px-6 py-5 border-b border-border">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-primary to-secondary flex items-center justify-center">
              <span className="text-white font-bold text-lg">T</span>
            </div>
            <span className="text-xl font-bold gradient-text">Testiva</span>
            <button onClick={() => setSidebarOpen(false)} className="lg:hidden ml-auto text-text-secondary hover:text-white">
              <HiOutlineX size={20} />
            </button>
          </div>

          {/* Nav */}
          <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
            {adminLinks.map((link) => {
              const isActive = location.pathname === link.path;
              return (
                <Link
                  key={link.path}
                  to={link.path}
                  onClick={() => setSidebarOpen(false)}
                  className={`flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200 ${
                    isActive
                      ? 'bg-gradient-to-r from-primary/20 to-primary/5 text-primary-light border-l-2 border-primary'
                      : 'text-text-secondary hover:text-white hover:bg-white/5'
                  }`}
                >
                  <link.icon size={20} />
                  {link.label}
                </Link>
              );
            })}
          </nav>

          {/* User + Logout */}
          <div className="p-4 border-t border-border">
            <div className="flex items-center gap-3 mb-3 px-2">
              <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary to-secondary flex items-center justify-center text-xs font-bold text-white">
                {user?.name?.charAt(0) || 'A'}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-white truncate">{user?.name || 'Admin'}</p>
                <p className="text-xs text-text-secondary truncate">{user?.email || ''}</p>
              </div>
            </div>
            <button onClick={handleLogout} className="flex items-center gap-2 w-full px-4 py-2.5 rounded-xl text-sm text-red-400 hover:bg-red-500/10 transition-colors">
              <HiOutlineLogout size={18} />
              Sign Out
            </button>
          </div>
        </div>
      </aside>

      {/* Overlay for mobile */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-30 bg-black/50 lg:hidden" onClick={() => setSidebarOpen(false)} />
      )}

      {/* Main */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top bar */}
        <header className="h-16 border-b border-border flex items-center px-6 bg-surface/50 backdrop-blur-xl">
          <button onClick={() => setSidebarOpen(true)} className="lg:hidden mr-4 text-text-secondary hover:text-white">
            <HiOutlineMenu size={24} />
          </button>
          <h1 className="text-lg font-semibold text-white">
            {adminLinks.find(l => l.path === location.pathname)?.label || 'Admin Panel'}
          </h1>
        </header>

        {/* Content */}
        <main className="flex-1 overflow-y-auto p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
