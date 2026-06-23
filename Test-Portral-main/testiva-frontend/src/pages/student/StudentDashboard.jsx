import { useState, useEffect } from 'react';
import { studentAPI } from '../../services/api';
import { HiOutlineAcademicCap, HiOutlineClipboardCheck, HiOutlineTrendingUp } from 'react-icons/hi';

export default function StudentDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    studentAPI.getDashboard().then(res => { setData(res.data); setLoading(false); }).catch(() => setLoading(false));
  }, []);

  if (loading) return <div className="flex justify-center py-20"><div className="spinner" /></div>;
  if (!data) return <p className="text-text-secondary text-center py-10">Failed to load</p>;

  const { student, testCount, latestResult, results } = data;

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="glass-card p-6 relative overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 rounded-full bg-primary/10 blur-[80px] -translate-y-1/2 translate-x-1/2" />
        <div className="relative z-10">
          <h2 className="text-2xl font-bold text-white mb-1">Welcome back, {student.name}! 👋</h2>
          <p className="text-text-secondary">{student.course} • {student.branch} • {student.year}</p>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="glass-card-hover p-5">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center shadow-lg shadow-blue-500/20">
              <HiOutlineClipboardCheck className="text-white" size={20} />
            </div>
          </div>
          <p className="text-2xl font-bold text-white">{testCount}</p>
          <p className="text-xs text-text-secondary mt-1">Tests Taken</p>
        </div>

        {latestResult && (
          <>
            <div className="glass-card-hover p-5">
              <div className="flex items-center gap-3 mb-3">
                <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-emerald-500 to-green-500 flex items-center justify-center shadow-lg shadow-emerald-500/20">
                  <HiOutlineTrendingUp className="text-white" size={20} />
                </div>
              </div>
              <p className="text-2xl font-bold text-white">{latestResult.totalScore}/{latestResult.totalMarks}</p>
              <p className="text-xs text-text-secondary mt-1">Latest Score</p>
            </div>
            <div className="glass-card-hover p-5">
              <div className="flex items-center gap-3 mb-3">
                <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-purple-500 to-violet-500 flex items-center justify-center shadow-lg shadow-purple-500/20">
                  <HiOutlineAcademicCap className="text-white" size={20} />
                </div>
              </div>
              <p className="text-2xl font-bold text-white">{latestResult.testName}</p>
              <p className="text-xs text-text-secondary mt-1">Last Test</p>
            </div>
          </>
        )}
      </div>

      {/* Recent Results */}
      {results && results.length > 0 && (
        <div className="glass-card p-6">
          <h3 className="text-lg font-semibold text-white mb-4">Test History</h3>
          <div className="space-y-3">
            {results.map((r, i) => {
              const pct = r.totalMarks > 0 ? Math.round(r.totalScore * 100 / r.totalMarks) : 0;
              return (
                <div key={i} className="flex items-center justify-between p-4 rounded-xl bg-white/3 hover:bg-white/5 transition-colors">
                  <div className="flex-1">
                    <p className="text-sm font-medium text-white">{r.testName}</p>
                    <p className="text-xs text-text-secondary mt-0.5">{r.testId} • {r.submittedAt ? new Date(r.submittedAt).toLocaleDateString() : ''}</p>
                  </div>
                  <div className="flex items-center gap-3">
                    <div className="w-24 h-2 bg-white/5 rounded-full overflow-hidden">
                      <div className={`h-full rounded-full ${pct >= 75 ? 'bg-green-500' : pct >= 50 ? 'bg-amber-500' : 'bg-red-500'}`} style={{ width: `${pct}%` }} />
                    </div>
                    <span className={`text-sm font-bold min-w-[3rem] text-right ${pct >= 75 ? 'text-green-400' : pct >= 50 ? 'text-amber-400' : 'text-red-400'}`}>
                      {pct}%
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
