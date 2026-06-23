import { useState, useEffect } from 'react';
import { adminAPI } from '../../services/api';
import toast from 'react-hot-toast';
import { HiOutlineSearch, HiOutlineShieldCheck, HiOutlineBan } from 'react-icons/hi';

export default function ManageStudents() {
  const [students, setStudents] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  const load = () => {
    adminAPI.getStudents().then(res => {
      setStudents(res.data);
      setLoading(false);
    }).catch(() => setLoading(false));
  };

  useEffect(load, []);

  const toggleStatus = async (id) => {
    try {
      const res = await adminAPI.toggleStudentStatus(id);
      toast.success(`Status → ${res.data.newStatus}`);
      load();
    } catch { toast.error('Failed to update'); }
  };

  const filtered = students.filter(s =>
    s.name?.toLowerCase().includes(search.toLowerCase()) ||
    s.email?.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className="flex justify-center py-20"><div className="spinner" /></div>;

  return (
    <div className="space-y-4">
      {/* Search */}
      <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center justify-between">
        <div className="relative w-full sm:w-80">
          <HiOutlineSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-text-secondary" size={18} />
          <input type="text" className="input-field pl-11" placeholder="Search students..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <span className="text-sm text-text-secondary">{filtered.length} students</span>
      </div>

      {/* Table */}
      <div className="glass-card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="table-modern">
            <thead>
              <tr>
                <th>#</th>
                <th>Name</th>
                <th>Email</th>
                <th>Contact</th>
                <th>Course</th>
                <th>Branch</th>
                <th>Year</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((s, i) => (
                <tr key={s.id}>
                  <td className="text-text-secondary">{i + 1}</td>
                  <td className="font-medium">{s.name}</td>
                  <td className="text-text-secondary">{s.email}</td>
                  <td>{s.contactno}</td>
                  <td>{s.course}</td>
                  <td>{s.branch}</td>
                  <td>{s.year}</td>
                  <td>
                    <span className={`status-badge ${
                      s.status === 'VERIFIED' ? 'bg-green-500/15 text-green-400' :
                      s.status === 'PENDING' ? 'bg-amber-500/15 text-amber-400' :
                      'bg-red-500/15 text-red-400'
                    }`}>
                      <span className="w-1.5 h-1.5 rounded-full bg-current" />
                      {s.status}
                    </span>
                  </td>
                  <td>
                    <button onClick={() => toggleStatus(s.id)} className={`${s.status === 'VERIFIED' ? 'btn-danger' : 'btn-success'} text-xs py-1.5 px-3`}>
                      {s.status === 'VERIFIED' ? <><HiOutlineBan size={14} /> Disable</> :
                       s.status === 'PENDING' ? <><HiOutlineShieldCheck size={14} /> Verify</> :
                       <><HiOutlineShieldCheck size={14} /> Enable</>}
                    </button>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan={9} className="text-center text-text-secondary py-8">No students found</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
