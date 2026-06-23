import { useState, useEffect } from 'react';
import { adminAPI } from '../../services/api';
import toast from 'react-hot-toast';
import { HiOutlineSearch, HiOutlineTrash, HiOutlinePlus, HiOutlineX, HiOutlineCheck } from 'react-icons/hi';

export default function QuestionBank() {
  const [questions, setQuestions] = useState([]);
  const [tests, setTests] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState([]);
  const [showAdd, setShowAdd] = useState(false);
  const [showAssign, setShowAssign] = useState(false);
  const [assignTestId, setAssignTestId] = useState('');
  const [successTestId, setSuccessTestId] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [addForm, setAddForm] = useState({ question: '', a: '', b: '', c: '', d: '', correct: '', course: '', branch: '', year: '' });

  const load = () => {
    Promise.all([adminAPI.getQuestions(), adminAPI.getTests()]).then(([qRes, tRes]) => {
      setQuestions(qRes.data);
      setTests(tRes.data);
      setLoading(false);
    }).catch(() => setLoading(false));
  };

  useEffect(load, []);

  const toggleSelect = (id) => {
    setSelected(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);
  };

  const selectAll = () => {
    if (selected.length === filtered.length) setSelected([]);
    else setSelected(filtered.map(q => q.id));
  };

  const deleteQuestion = async (id) => {
    try {
      await adminAPI.deleteQuestion(id);
      toast.success('Deleted');
      load();
    } catch { toast.error('Failed'); }
  };

  const handleAdd = async (e) => {
    e.preventDefault();
    if (!addForm.question || !addForm.a || !addForm.b || !addForm.c || !addForm.d || !addForm.correct) {
      return toast.error('Fill all required fields');
    }
    setSubmitting(true);
    try {
      await adminAPI.addQuestion(addForm);
      toast.success('Question added');
      setShowAdd(false);
      setAddForm({ question: '', a: '', b: '', c: '', d: '', correct: '', course: '', branch: '', year: '' });
      load();
    } catch (err) { toast.error(err.response?.data?.error || 'Failed'); }
    finally { setSubmitting(false); }
  };

  const handleAssign = async () => {
    if (!assignTestId) return toast.error('Select a test');
    const test = tests.find(t => t.testId === assignTestId);
    if (test && selected.length !== test.numberOfQuestions) {
      return toast.error(`Select exactly ${test.numberOfQuestions} questions (selected: ${selected.length})`);
    }
    setSubmitting(true);
    try {
      const res = await adminAPI.assignQuestions(assignTestId, selected);
      setSuccessTestId(res.data.testId);
      setShowAssign(false);
      setSelected([]);
      load();
    } catch (err) { toast.error(err.response?.data?.error || 'Failed'); }
    finally { setSubmitting(false); }
  };

  const filtered = questions.filter(q =>
    q.question?.toLowerCase().includes(search.toLowerCase()) ||
    q.course?.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className="flex justify-center py-20"><div className="spinner" /></div>;

  return (
    <div className="space-y-4">
      {/* Toolbar */}
      <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center justify-between">
        <div className="relative w-full sm:w-80">
          <HiOutlineSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-text-secondary" size={18} />
          <input type="text" className="input-field pl-11" placeholder="Search questions..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <div className="flex gap-2">
          {selected.length > 0 && (
            <button onClick={() => setShowAssign(true)} className="btn-success">
              <HiOutlineCheck size={16} /> Assign {selected.length} to Test
            </button>
          )}
          <button onClick={() => setShowAdd(true)} className="btn-primary">
            <HiOutlinePlus size={18} /> Add Question
          </button>
        </div>
      </div>

      {/* Add Question Modal */}
      {showAdd && (
        <div className="modal-overlay" onClick={() => setShowAdd(false)}>
          <div className="modal-content max-w-lg" onClick={e => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-5">
              <h3 className="text-lg font-semibold text-white">Add Question</h3>
              <button onClick={() => setShowAdd(false)} className="text-text-secondary hover:text-white"><HiOutlineX size={20} /></button>
            </div>
            <form onSubmit={handleAdd} className="space-y-3">
              <div>
                <label className="block text-sm text-text-secondary mb-1">Question</label>
                <textarea className="input-field" rows={2} value={addForm.question} onChange={e => setAddForm({...addForm, question: e.target.value})} />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div><label className="block text-xs text-text-secondary mb-1">Option A</label>
                  <input className="input-field" value={addForm.a} onChange={e => setAddForm({...addForm, a: e.target.value})} /></div>
                <div><label className="block text-xs text-text-secondary mb-1">Option B</label>
                  <input className="input-field" value={addForm.b} onChange={e => setAddForm({...addForm, b: e.target.value})} /></div>
                <div><label className="block text-xs text-text-secondary mb-1">Option C</label>
                  <input className="input-field" value={addForm.c} onChange={e => setAddForm({...addForm, c: e.target.value})} /></div>
                <div><label className="block text-xs text-text-secondary mb-1">Option D</label>
                  <input className="input-field" value={addForm.d} onChange={e => setAddForm({...addForm, d: e.target.value})} /></div>
              </div>
              <div>
                <label className="block text-sm text-text-secondary mb-1">Correct Answer</label>
                <select className="input-field" value={addForm.correct} onChange={e => setAddForm({...addForm, correct: e.target.value})}>
                  <option value="">Select</option>
                  <option value="A">A</option><option value="B">B</option><option value="C">C</option><option value="D">D</option>
                </select>
              </div>
              <div className="grid grid-cols-3 gap-3">
                <input className="input-field" placeholder="Course" value={addForm.course} onChange={e => setAddForm({...addForm, course: e.target.value})} />
                <input className="input-field" placeholder="Branch" value={addForm.branch} onChange={e => setAddForm({...addForm, branch: e.target.value})} />
                <input className="input-field" placeholder="Year" value={addForm.year} onChange={e => setAddForm({...addForm, year: e.target.value})} />
              </div>
              <button type="submit" disabled={submitting} className="btn-primary w-full justify-center">
                {submitting ? <div className="spinner w-5 h-5 border-2" /> : 'Add Question'}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Assign Modal */}
      {showAssign && (
        <div className="modal-overlay" onClick={() => setShowAssign(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h3 className="text-lg font-semibold text-white mb-4">Assign to Test</h3>
            <p className="text-sm text-text-secondary mb-4">{selected.length} questions selected</p>
            <select className="input-field mb-4" value={assignTestId} onChange={e => setAssignTestId(e.target.value)}>
              <option value="">Select a test...</option>
              {tests.filter(t => t.testId?.startsWith('PENDING')).map(t => (
                <option key={t.id} value={t.testId}>{t.testName} ({t.numberOfQuestions} needed)</option>
              ))}
            </select>
            <div className="flex gap-2">
              <button onClick={handleAssign} disabled={submitting} className="btn-primary flex-1 justify-center">
                {submitting ? <div className="spinner w-5 h-5 border-2" /> : 'Assign'}
              </button>
              <button onClick={() => setShowAssign(false)} className="btn-outline flex-1 justify-center">Cancel</button>
            </div>
          </div>
        </div>
      )}

      {/* Success Modal */}
      {successTestId && (
        <div className="modal-overlay" onClick={() => setSuccessTestId('')}>
          <div className="modal-content text-center" onClick={e => e.stopPropagation()}>
            <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-green-500/15 flex items-center justify-center">
              <HiOutlineCheck className="text-green-400" size={32} />
            </div>
            <h3 className="text-xl font-bold text-white mb-2">Test Scheduled Successfully!</h3>
            <p className="text-text-secondary mb-2">Your test ID is:</p>
            <p className="text-2xl font-mono font-bold text-primary-light bg-primary/10 px-4 py-2 rounded-xl inline-block mb-4">{successTestId}</p>
            <br />
            <button onClick={() => setSuccessTestId('')} className="btn-primary mt-2">Done</button>
          </div>
        </div>
      )}

      {/* Table */}
      <div className="glass-card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="table-modern">
            <thead>
              <tr>
                <th><input type="checkbox" onChange={selectAll} checked={selected.length === filtered.length && filtered.length > 0} className="rounded" /></th>
                <th>#</th>
                <th>Question</th>
                <th>A</th>
                <th>B</th>
                <th>C</th>
                <th>D</th>
                <th>Ans</th>
                <th>Course</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((q, i) => (
                <tr key={q.id} className={selected.includes(q.id) ? 'bg-primary/5' : ''}>
                  <td><input type="checkbox" checked={selected.includes(q.id)} onChange={() => toggleSelect(q.id)} className="rounded" /></td>
                  <td className="text-text-secondary">{i + 1}</td>
                  <td className="max-w-xs truncate font-medium">{q.question}</td>
                  <td className="text-text-secondary text-xs max-w-[100px] truncate">{q.a}</td>
                  <td className="text-text-secondary text-xs max-w-[100px] truncate">{q.b}</td>
                  <td className="text-text-secondary text-xs max-w-[100px] truncate">{q.c}</td>
                  <td className="text-text-secondary text-xs max-w-[100px] truncate">{q.d}</td>
                  <td><span className="bg-green-500/15 text-green-400 text-xs font-bold px-2 py-0.5 rounded">{q.correct}</span></td>
                  <td className="text-text-secondary text-xs">{q.course}</td>
                  <td>
                    <button onClick={() => deleteQuestion(q.id)} className="text-red-400 hover:text-red-300 p-1.5 hover:bg-red-500/10 rounded-lg transition-colors">
                      <HiOutlineTrash size={16} />
                    </button>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan={10} className="text-center py-8 text-text-secondary">No questions found</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
