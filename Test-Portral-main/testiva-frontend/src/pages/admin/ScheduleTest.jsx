import { useState, useEffect } from 'react';
import { adminAPI } from '../../services/api';
import toast from 'react-hot-toast';
import { HiOutlinePlus, HiOutlineTrash, HiOutlineClock, HiOutlineX } from 'react-icons/hi';

export default function ScheduleTest() {
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({
    testName: '', course: '', branch: '', year: '', testDuration: '', numberOfQuestions: '', startTime: ''
  });

  const load = () => {
    adminAPI.getTests().then(res => { setTests(res.data); setLoading(false); }).catch(() => setLoading(false));
  };

  useEffect(load, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.testName || !form.course || !form.branch || !form.year || !form.testDuration || !form.numberOfQuestions || !form.startTime) {
      return toast.error('Fill all fields');
    }
    setSubmitting(true);
    try {
      const res = await adminAPI.scheduleTest(form);
      toast.success('Test scheduled! Now assign questions from Question Bank.');
      setShowForm(false);
      setForm({ testName: '', course: '', branch: '', year: '', testDuration: '', numberOfQuestions: '', startTime: '' });
      load();
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed');
    } finally { setSubmitting(false); }
  };

  const deleteTest = async (id) => {
    if (!confirm('Delete this test?')) return;
    try {
      await adminAPI.deleteTest(id);
      toast.success('Test deleted');
      load();
    } catch { toast.error('Failed'); }
  };

  if (loading) return <div className="flex justify-center py-20"><div className="spinner" /></div>;

  return (
    <div className="space-y-5">
      <div className="flex justify-between items-center">
        <p className="text-sm text-text-secondary">{tests.length} tests</p>
        <button onClick={() => setShowForm(true)} className="btn-primary">
          <HiOutlinePlus size={18} /> Schedule New Test
        </button>
      </div>

      {/* Form Modal */}
      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal-content max-w-lg" onClick={e => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-lg font-semibold text-white">Schedule New Test</h3>
              <button onClick={() => setShowForm(false)} className="text-text-secondary hover:text-white"><HiOutlineX size={20} /></button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm text-text-secondary mb-1.5">Test Name</label>
                <input className="input-field" placeholder="e.g. Data Structures Mid Term" value={form.testName} onChange={e => setForm({...form, testName: e.target.value})} />
              </div>
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-sm text-text-secondary mb-1.5">Course</label>
                  <input className="input-field" placeholder="B.Tech" value={form.course} onChange={e => setForm({...form, course: e.target.value})} />
                </div>
                <div>
                  <label className="block text-sm text-text-secondary mb-1.5">Branch</label>
                  <input className="input-field" placeholder="CSE" value={form.branch} onChange={e => setForm({...form, branch: e.target.value})} />
                </div>
                <div>
                  <label className="block text-sm text-text-secondary mb-1.5">Year</label>
                  <input className="input-field" placeholder="Third Year" value={form.year} onChange={e => setForm({...form, year: e.target.value})} />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm text-text-secondary mb-1.5">Duration (min)</label>
                  <input type="number" className="input-field" placeholder="30" value={form.testDuration} onChange={e => setForm({...form, testDuration: e.target.value})} />
                </div>
                <div>
                  <label className="block text-sm text-text-secondary mb-1.5">No. of Questions</label>
                  <input type="number" className="input-field" placeholder="20" value={form.numberOfQuestions} onChange={e => setForm({...form, numberOfQuestions: e.target.value})} />
                </div>
              </div>
              <div>
                <label className="block text-sm text-text-secondary mb-1.5">Start Time</label>
                <input type="datetime-local" className="input-field" value={form.startTime} onChange={e => setForm({...form, startTime: e.target.value})} />
              </div>
              <button type="submit" disabled={submitting} className="btn-primary w-full justify-center">
                {submitting ? <div className="spinner w-5 h-5 border-2" /> : 'Schedule Test'}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Test List */}
      <div className="glass-card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="table-modern">
            <thead>
              <tr>
                <th>Test ID</th>
                <th>Name</th>
                <th>Course / Branch</th>
                <th>Year</th>
                <th>Questions</th>
                <th>Duration</th>
                <th>Start Time</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {tests.map(t => (
                <tr key={t.id}>
                  <td>
                    <span className={`font-mono text-xs px-2 py-1 rounded-lg ${t.testId?.startsWith('PENDING') ? 'bg-amber-500/15 text-amber-400' : 'bg-primary/10 text-primary-light'}`}>
                      {t.testId}
                    </span>
                  </td>
                  <td className="font-medium">{t.testName}</td>
                  <td className="text-text-secondary">{t.course} / {t.branch}</td>
                  <td>{t.year}</td>
                  <td className="text-center">{t.numberOfQuestions}</td>
                  <td><HiOutlineClock className="inline mr-1" size={14} />{t.testDuration}m</td>
                  <td className="text-text-secondary text-xs">{t.startTime ? new Date(t.startTime).toLocaleString() : '—'}</td>
                  <td>
                    <span className={`status-badge ${
                      t.status === 'Active' ? 'bg-green-500/15 text-green-400' :
                      t.status === 'Scheduled' ? 'bg-blue-500/15 text-blue-400' :
                      t.status === 'Reminder_Sent' ? 'bg-purple-500/15 text-purple-400' :
                      'bg-gray-500/15 text-gray-400'
                    }`}>
                      <span className="w-1.5 h-1.5 rounded-full bg-current" />
                      {t.status?.replace('_', ' ')}
                    </span>
                  </td>
                  <td>
                    <button onClick={() => deleteTest(t.id)} className="btn-danger text-xs py-1.5 px-3">
                      <HiOutlineTrash size={14} />
                    </button>
                  </td>
                </tr>
              ))}
              {tests.length === 0 && (
                <tr><td colSpan={9} className="text-center py-8 text-text-secondary">No tests yet</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
