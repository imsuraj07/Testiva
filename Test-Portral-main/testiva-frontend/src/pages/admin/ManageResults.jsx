import { useState, useEffect } from 'react';
import { adminAPI } from '../../services/api';
import { HiOutlineSearch } from 'react-icons/hi';

export default function ManageResults() {
  const [results, setResults] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminAPI.getResults().then(res => { setResults(res.data); setLoading(false); }).catch(() => setLoading(false));
  }, []);

  const filtered = results.filter(r =>
    r.name?.toLowerCase().includes(search.toLowerCase()) ||
    r.testId?.toLowerCase().includes(search.toLowerCase()) ||
    r.email?.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className="flex justify-center py-20"><div className="spinner" /></div>;

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center justify-between">
        <div className="relative w-full sm:w-80">
          <HiOutlineSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-text-secondary" size={18} />
          <input type="text" className="input-field pl-11" placeholder="Search by name, email, test ID..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <span className="text-sm text-text-secondary">{filtered.length} results</span>
      </div>

      <div className="glass-card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="table-modern">
            <thead>
              <tr>
                <th>#</th>
                <th>Student</th>
                <th>Email</th>
                <th>Contact</th>
                <th>Course</th>
                <th>Branch</th>
                <th>Year</th>
                <th>Test ID</th>
                <th>Test Name</th>
                <th>Score</th>
                <th>Performance</th>
                <th>Submitted</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((r, i) => {
                const pct = r.totalMarks > 0 ? Math.round(r.totalScore * 100 / r.totalMarks) : 0;
                return (
                  <tr key={r.id}>
                    <td className="text-text-secondary">{i + 1}</td>
                    <td className="font-medium">{r.name}</td>
                    <td className="text-text-secondary text-xs">{r.email}</td>
                    <td>{r.contactno}</td>
                    <td>{r.course}</td>
                    <td>{r.branch}</td>
                    <td>{r.year}</td>
                    <td><span className="font-mono text-xs bg-primary/10 text-primary-light px-2 py-0.5 rounded-lg">{r.testId}</span></td>
                    <td>{r.testName}</td>
                    <td className="font-semibold">{r.totalScore} / {r.totalMarks}</td>
                    <td>
                      <div className="flex items-center gap-2">
                        <div className="w-20 h-2 bg-white/5 rounded-full overflow-hidden">
                          <div
                            className={`h-full rounded-full transition-all ${pct >= 75 ? 'bg-green-500' : pct >= 50 ? 'bg-amber-500' : 'bg-red-500'}`}
                            style={{ width: `${pct}%` }}
                          />
                        </div>
                        <span className={`text-xs font-semibold ${pct >= 75 ? 'text-green-400' : pct >= 50 ? 'text-amber-400' : 'text-red-400'}`}>
                          {pct}%
                        </span>
                      </div>
                    </td>
                    <td className="text-text-secondary text-xs">{r.submittedAt ? new Date(r.submittedAt).toLocaleString() : '—'}</td>
                  </tr>
                );
              })}
              {filtered.length === 0 && (
                <tr><td colSpan={12} className="text-center py-8 text-text-secondary">No results found</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
