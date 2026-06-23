import { useState, useEffect } from 'react';
import { studentAPI } from '../../services/api';

export default function StudentResults() {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    studentAPI.getResults().then(res => { setResults(res.data); setLoading(false); }).catch(() => setLoading(false));
  }, []);

  if (loading) return <div className="flex justify-center py-20"><div className="spinner" /></div>;

  return (
    <div className="space-y-4">
      {results.length > 0 ? (
        <div className="glass-card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="table-modern">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Test ID</th>
                  <th>Test Name</th>
                  <th>Score</th>
                  <th>Performance</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {results.map((r, i) => {
                  const pct = r.totalMarks > 0 ? Math.round(r.totalScore * 100 / r.totalMarks) : 0;
                  return (
                    <tr key={r.id}>
                      <td className="text-text-secondary">{i + 1}</td>
                      <td><span className="font-mono text-xs bg-primary/10 text-primary-light px-2 py-0.5 rounded-lg">{r.testId}</span></td>
                      <td className="font-medium">{r.testName}</td>
                      <td className="font-semibold">{r.totalScore} / {r.totalMarks}</td>
                      <td>
                        <div className="flex items-center gap-2">
                          <div className="w-20 h-2 bg-white/5 rounded-full overflow-hidden">
                            <div className={`h-full rounded-full ${pct >= 75 ? 'bg-green-500' : pct >= 50 ? 'bg-amber-500' : 'bg-red-500'}`} style={{ width: `${pct}%` }} />
                          </div>
                          <span className={`text-xs font-semibold ${pct >= 75 ? 'text-green-400' : pct >= 50 ? 'text-amber-400' : 'text-red-400'}`}>{pct}%</span>
                        </div>
                      </td>
                      <td className="text-text-secondary text-xs">{r.submittedAt ? new Date(r.submittedAt).toLocaleString() : '—'}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="glass-card p-12 text-center">
          <p className="text-text-secondary text-lg">No results yet</p>
          <p className="text-text-secondary text-sm mt-1">Take a test to see your results here.</p>
        </div>
      )}
    </div>
  );
}
