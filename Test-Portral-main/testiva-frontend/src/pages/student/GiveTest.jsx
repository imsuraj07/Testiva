import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { studentAPI } from '../../services/api';
import toast from 'react-hot-toast';
import { HiOutlineClock, HiOutlinePlay, HiOutlineX, HiOutlineLockClosed } from 'react-icons/hi';

export default function GiveTest() {
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [testIdInput, setTestIdInput] = useState('');
  const [validating, setValidating] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    studentAPI.getTests().then(res => { setTests(res.data); setLoading(false); }).catch(() => setLoading(false));
  }, []);

  const handleStartTest = async () => {
    if (!testIdInput.trim()) return toast.error('Enter a Test ID');
    setValidating(true);
    try {
      const res = await studentAPI.validateTest(testIdInput.trim());
      if (res.data.valid) {
        navigate(`/student/test/${res.data.testDbId}`);
      }
    } catch (err) {
      toast.error(err.response?.data?.error || 'Invalid Test ID');
    } finally {
      setValidating(false);
    }
  };

  if (loading) return <div className="flex justify-center py-20"><div className="spinner" /></div>;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <p className="text-text-secondary text-sm">{tests.length} tests available</p>
        <button onClick={() => setShowModal(true)} className="btn-primary">
          <HiOutlinePlay size={18} /> Enter Test ID
        </button>
      </div>

      {/* Enter Test ID Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-5">
              <h3 className="text-lg font-semibold text-white">Enter Test ID</h3>
              <button onClick={() => setShowModal(false)} className="text-text-secondary hover:text-white"><HiOutlineX size={20} /></button>
            </div>
            <p className="text-sm text-text-secondary mb-4">Enter the Test ID provided by your instructor to start the exam.</p>
            <input
              className="input-field mb-4"
              placeholder="e.g. TTP001"
              value={testIdInput}
              onChange={e => setTestIdInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleStartTest()}
              autoFocus
            />
            <button onClick={handleStartTest} disabled={validating} className="btn-primary w-full justify-center">
              {validating ? <div className="spinner w-5 h-5 border-2" /> : <><HiOutlinePlay size={18} /> Start Test</>}
            </button>
          </div>
        </div>
      )}

      {/* Test Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
        {tests.map(t => (
          <div key={t.id} className="glass-card-hover p-5 flex flex-col">
            <div className="flex items-start justify-between mb-3">
              <div>
                <h3 className="font-semibold text-white text-base">{t.testName}</h3>
                <p className="text-xs text-text-secondary mt-0.5">{t.course} • {t.branch} • {t.year}</p>
              </div>
              <span className={`status-badge ${
                t.status === 'Active' ? 'bg-green-500/15 text-green-400' :
                t.status === 'Scheduled' ? 'bg-blue-500/15 text-blue-400' :
                'bg-gray-500/15 text-gray-400'
              }`}>
                <span className="w-1.5 h-1.5 rounded-full bg-current" />
                {t.status?.replace('_', ' ')}
              </span>
            </div>

            <div className="flex gap-4 text-xs text-text-secondary mb-4">
              <span className="flex items-center gap-1"><HiOutlineClock size={14} /> {t.testDuration} min</span>
              <span>{t.numberOfQuestions} questions</span>
            </div>

            <div className="text-xs text-text-secondary mb-4">
              <span>Start: {new Date(t.startTime).toLocaleString()}</span>
            </div>

            <div className="mt-auto">
              {t.alreadyAttempted ? (
                <button disabled className="btn-outline w-full justify-center opacity-50 cursor-not-allowed">
                  <HiOutlineLockClosed size={16} /> Already Attempted
                </button>
              ) : (
                <button
                  onClick={() => { setTestIdInput(t.testId); setShowModal(true); }}
                  className="btn-primary w-full justify-center"
                >
                  <HiOutlinePlay size={16} /> Start Test
                </button>
              )}
            </div>
          </div>
        ))}
        {tests.length === 0 && (
          <div className="col-span-full text-center py-12 text-text-secondary">
            <p className="text-lg mb-2">No tests available</p>
            <p className="text-sm">Check back later or contact your instructor.</p>
          </div>
        )}
      </div>
    </div>
  );
}
