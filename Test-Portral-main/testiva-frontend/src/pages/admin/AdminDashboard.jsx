import { useState, useEffect } from 'react';
import { adminAPI } from '../../services/api';
import { HiOutlineUsers, HiOutlineClipboardCheck, HiOutlineChartBar, HiOutlineQuestionMarkCircle, HiOutlineCalendar } from 'react-icons/hi';

export default function AdminDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminAPI.getDashboard().then(res => {
      setData(res.data);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  if (loading) return <div className="flex justify-center py-20"><div className="spinner" /></div>;
  if (!data) return <p className="text-text-secondary text-center py-10">Failed to load dashboard</p>;

  const statCards = [
    { label: 'Total Students', value: data.totalStudents, icon: HiOutlineUsers, color: 'from-blue-500 to-blue-600', shadow: 'shadow-blue-500/20' },
    { label: 'Total Tests', value: data.totalTests, icon: HiOutlineCalendar, color: 'from-purple-500 to-purple-600', shadow: 'shadow-purple-500/20' },
    { label: 'Total Questions', value: data.totalQuestions, icon: HiOutlineQuestionMarkCircle, color: 'from-amber-500 to-orange-500', shadow: 'shadow-amber-500/20' },
    { label: 'Total Results', value: data.totalResults, icon: HiOutlineChartBar, color: 'from-emerald-500 to-green-500', shadow: 'shadow-emerald-500/20' },
    { label: 'Enquiries', value: data.totalEnquiries, icon: HiOutlineClipboardCheck, color: 'from-rose-500 to-pink-500', shadow: 'shadow-rose-500/20' },
  ];

  return (
    <div className="space-y-6">
      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4">
        {statCards.map((card) => (
          <div key={card.label} className="glass-card-hover p-5">
            <div className="flex items-center justify-between mb-3">
              <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${card.color} ${card.shadow} flex items-center justify-center shadow-lg`}>
                <card.icon className="text-white" size={20} />
              </div>
            </div>
            <p className="text-2xl font-bold text-white">{card.value}</p>
            <p className="text-xs text-text-secondary mt-1">{card.label}</p>
          </div>
        ))}
      </div>

      {/* Recent Tests */}
      <div className="glass-card p-6">
        <h2 className="text-lg font-semibold text-white mb-4">Recent Tests</h2>
        {data.recentTests?.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="table-modern">
              <thead>
                <tr>
                  <th>Test ID</th>
                  <th>Test Name</th>
                  <th>Course</th>
                  <th>Branch</th>
                  <th>Status</th>
                  <th>Duration</th>
                </tr>
              </thead>
              <tbody>
                {data.recentTests.slice(0, 8).map((t) => (
                  <tr key={t.id}>
                    <td><span className="font-mono text-primary-light text-xs bg-primary/10 px-2 py-1 rounded-lg">{t.testId}</span></td>
                    <td className="font-medium">{t.testName}</td>
                    <td>{t.course}</td>
                    <td>{t.branch}</td>
                    <td>
                      <span className={`status-badge ${
                        t.status === 'Active' ? 'bg-green-500/15 text-green-400' :
                        t.status === 'Scheduled' ? 'bg-blue-500/15 text-blue-400' :
                        'bg-gray-500/15 text-gray-400'
                      }`}>
                        <span className="w-1.5 h-1.5 rounded-full bg-current" />
                        {t.status.replace('_', ' ')}
                      </span>
                    </td>
                    <td>{t.testDuration} min</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-text-secondary text-sm py-4 text-center">No tests scheduled yet</p>
        )}
      </div>

      {/* Recent Enquiries */}
      <div className="glass-card p-6">
        <h2 className="text-lg font-semibold text-white mb-4">Recent Enquiries</h2>
        {data.recentEnquiries?.length > 0 ? (
          <div className="space-y-3">
            {data.recentEnquiries.map((e) => (
              <div key={e.id} className="flex items-center justify-between p-3 rounded-xl bg-white/3 hover:bg-white/5 transition-colors">
                <div>
                  <p className="text-sm font-medium text-white">{e.name}</p>
                  <p className="text-xs text-text-secondary">{e.email}</p>
                </div>
                <span className={`status-badge ${e.status === 'PENDING' ? 'bg-amber-500/15 text-amber-400' : 'bg-green-500/15 text-green-400'}`}>
                  {e.status}
                </span>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-text-secondary text-sm py-4 text-center">No enquiries yet</p>
        )}
      </div>
    </div>
  );
}
