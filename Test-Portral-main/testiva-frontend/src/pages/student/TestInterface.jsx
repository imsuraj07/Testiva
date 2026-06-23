import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { studentAPI } from '../../services/api';
import toast from 'react-hot-toast';
import { HiOutlineChevronLeft, HiOutlineChevronRight, HiOutlineClock, HiOutlineCheck, HiOutlineX } from 'react-icons/hi';

export default function TestInterface() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [testData, setTestData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [currentQ, setCurrentQ] = useState(0);
  const [answers, setAnswers] = useState({});
  const [timeLeft, setTimeLeft] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState(null);
  const [showConfirm, setShowConfirm] = useState(false);
  const timerRef = useRef(null);

  useEffect(() => {
    studentAPI.startTest(id).then(res => {
      setTestData(res.data);
      setTimeLeft(res.data.testDuration * 60);
      setLoading(false);
    }).catch(err => {
      toast.error(err.response?.data?.error || 'Failed to load test');
      navigate('/student/give-test');
    });
  }, [id, navigate]);

  // Timer
  useEffect(() => {
    if (!testData || result) return;
    timerRef.current = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          clearInterval(timerRef.current);
          handleSubmit(true);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timerRef.current);
  }, [testData, result]);

  const handleSubmit = useCallback(async (auto = false) => {
    if (submitting) return;
    setSubmitting(true);
    clearInterval(timerRef.current);
    try {
      const res = await studentAPI.submitTest(testData.testId, answers);
      setResult(res.data);
      if (auto) toast('Time up! Test auto-submitted.', { icon: '⏰' });
      else toast.success('Test submitted!');
    } catch (err) {
      toast.error(err.response?.data?.error || 'Submit failed');
    } finally {
      setSubmitting(false);
    }
  }, [testData, answers, submitting]);

  const selectAnswer = (qId, option) => {
    setAnswers(prev => ({ ...prev, [qId]: option }));
  };

  const formatTime = (s) => {
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return `${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`;
  };

  if (loading) return (
    <div className="min-h-screen bg-bg flex items-center justify-center">
      <div className="text-center">
        <div className="spinner mx-auto mb-4" />
        <p className="text-text-secondary">Loading test...</p>
      </div>
    </div>
  );

  // ===================== RESULT VIEW =====================
  if (result) {
    const pct = result.totalMarks > 0 ? Math.round(result.totalScore * 100 / result.totalMarks) : 0;
    return (
      <div className="min-h-screen bg-bg p-4 sm:p-8">
        <div className="max-w-3xl mx-auto space-y-6">
          {/* Score Card */}
          <div className="glass-card p-8 text-center relative overflow-hidden">
            <div className="absolute top-0 left-1/2 -translate-x-1/2 w-96 h-96 rounded-full bg-primary/10 blur-[100px] -translate-y-1/2" />
            <div className="relative z-10">
              <div className={`w-24 h-24 mx-auto mb-4 rounded-full flex items-center justify-center text-3xl font-bold ${
                pct >= 75 ? 'bg-green-500/15 text-green-400' : pct >= 50 ? 'bg-amber-500/15 text-amber-400' : 'bg-red-500/15 text-red-400'
              }`}>
                {pct}%
              </div>
              <h2 className="text-2xl font-bold text-white mb-1">{result.testName}</h2>
              <p className="text-text-secondary mb-6">Test Completed</p>

              <div className="grid grid-cols-3 gap-4 max-w-sm mx-auto">
                <div className="glass-card p-3">
                  <p className="text-xl font-bold text-white">{result.totalScore}</p>
                  <p className="text-xs text-text-secondary">Correct</p>
                </div>
                <div className="glass-card p-3">
                  <p className="text-xl font-bold text-white">{result.totalMarks - result.totalScore}</p>
                  <p className="text-xs text-text-secondary">Wrong</p>
                </div>
                <div className="glass-card p-3">
                  <p className="text-xl font-bold text-white">{result.totalMarks}</p>
                  <p className="text-xs text-text-secondary">Total</p>
                </div>
              </div>

              <button onClick={() => navigate('/student/dashboard')} className="btn-primary mt-6">
                Back to Dashboard
              </button>
            </div>
          </div>

          {/* Question Review */}
          <div className="glass-card p-6">
            <h3 className="text-lg font-semibold text-white mb-4">Question Review</h3>
            <div className="space-y-4">
              {result.review?.map((q, i) => (
                <div key={i} className={`p-4 rounded-xl border ${q.isCorrect ? 'border-green-500/20 bg-green-500/5' : 'border-red-500/20 bg-red-500/5'}`}>
                  <div className="flex items-start gap-3 mb-3">
                    <span className={`w-6 h-6 rounded-full flex items-center justify-center text-xs ${q.isCorrect ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'}`}>
                      {q.isCorrect ? <HiOutlineCheck size={14} /> : <HiOutlineX size={14} />}
                    </span>
                    <p className="text-sm text-white flex-1"><span className="text-text-secondary">Q{i + 1}.</span> {q.question}</p>
                  </div>
                  <div className="grid grid-cols-2 gap-2 ml-9">
                    {['A', 'B', 'C', 'D'].map(opt => {
                      const isCorrect = q.correct === opt;
                      const isSelected = q.selected === opt;
                      return (
                        <div key={opt} className={`px-3 py-2 rounded-lg text-xs ${
                          isCorrect ? 'bg-green-500/15 text-green-400 border border-green-500/30' :
                          isSelected && !isCorrect ? 'bg-red-500/15 text-red-400 border border-red-500/30' :
                          'bg-white/3 text-text-secondary border border-transparent'
                        }`}>
                          <span className="font-semibold mr-1">{opt}.</span> {q[opt.toLowerCase()]}
                        </div>
                      );
                    })}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  // ===================== TEST UI =====================
  const questions = testData.questions;
  const q = questions[currentQ];
  const answeredCount = Object.keys(answers).length;
  const isUrgent = timeLeft < 60;

  return (
    <div className="min-h-screen bg-bg flex flex-col">
      {/* Top Bar */}
      <header className="h-14 bg-surface border-b border-border flex items-center justify-between px-4 sm:px-6 shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-primary to-secondary flex items-center justify-center">
            <span className="text-white font-bold text-sm">T</span>
          </div>
          <h1 className="text-sm sm:text-base font-semibold text-white truncate max-w-[200px]">{testData.testName}</h1>
        </div>
        <div className={`flex items-center gap-2 px-3 py-1.5 rounded-lg font-mono text-sm font-bold ${
          isUrgent ? 'bg-red-500/15 text-red-400 animate-pulse' : 'bg-primary/10 text-primary-light'
        }`}>
          <HiOutlineClock size={16} />
          {formatTime(timeLeft)}
        </div>
      </header>

      <div className="flex-1 flex overflow-hidden">
        {/* Question Area */}
        <div className="flex-1 flex flex-col p-4 sm:p-6 overflow-y-auto">
          {/* Question */}
          <div className="glass-card p-6 mb-6 flex-1">
            <div className="flex items-center gap-2 mb-4">
              <span className="bg-primary/15 text-primary-light text-xs font-bold px-3 py-1 rounded-lg">
                Question {currentQ + 1} of {questions.length}
              </span>
            </div>
            <h2 className="text-lg sm:text-xl font-medium text-white mb-6 leading-relaxed">{q.question}</h2>

            <div className="space-y-3">
              {['A', 'B', 'C', 'D'].map(opt => {
                const isSelected = answers[q.id] === opt;
                return (
                  <button
                    key={opt}
                    onClick={() => selectAnswer(q.id, opt)}
                    className={`w-full text-left p-4 rounded-xl border transition-all duration-200 flex items-center gap-3 ${
                      isSelected
                        ? 'border-primary bg-primary/10 shadow-lg shadow-primary/10'
                        : 'border-border hover:border-primary/30 hover:bg-white/3'
                    }`}
                  >
                    <span className={`w-8 h-8 rounded-lg flex items-center justify-center text-sm font-bold shrink-0 ${
                      isSelected ? 'bg-primary text-white' : 'bg-white/5 text-text-secondary'
                    }`}>
                      {opt}
                    </span>
                    <span className={`text-sm ${isSelected ? 'text-white' : 'text-text-secondary'}`}>
                      {q[opt.toLowerCase()]}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Navigation */}
          <div className="flex items-center justify-between">
            <button
              onClick={() => setCurrentQ(Math.max(0, currentQ - 1))}
              disabled={currentQ === 0}
              className="btn-outline disabled:opacity-30"
            >
              <HiOutlineChevronLeft size={18} /> Previous
            </button>

            {currentQ < questions.length - 1 ? (
              <button onClick={() => setCurrentQ(currentQ + 1)} className="btn-primary">
                Next <HiOutlineChevronRight size={18} />
              </button>
            ) : (
              <button onClick={() => setShowConfirm(true)} className="btn-success px-6 py-2.5">
                <HiOutlineCheck size={18} /> Submit Test
              </button>
            )}
          </div>
        </div>

        {/* Question Navigator (right panel) */}
        <aside className="hidden md:flex w-56 border-l border-border bg-surface/50 flex-col p-4 shrink-0">
          <p className="text-xs font-semibold text-text-secondary uppercase tracking-wider mb-3">Navigator</p>
          <div className="grid grid-cols-5 gap-2 mb-4">
            {questions.map((qq, i) => {
              const isAnswered = answers[qq.id] !== undefined;
              const isCurrent = i === currentQ;
              return (
                <button
                  key={qq.id}
                  onClick={() => setCurrentQ(i)}
                  className={`w-8 h-8 rounded-lg text-xs font-bold transition-all ${
                    isCurrent ? 'bg-primary text-white ring-2 ring-primary/50' :
                    isAnswered ? 'bg-green-500/20 text-green-400 border border-green-500/30' :
                    'bg-white/5 text-text-secondary border border-border hover:bg-white/10'
                  }`}
                >
                  {i + 1}
                </button>
              );
            })}
          </div>

          <div className="mt-auto space-y-2">
            <div className="flex items-center gap-2 text-xs text-text-secondary">
              <span className="w-3 h-3 rounded bg-green-500/20 border border-green-500/30" /> Answered ({answeredCount})
            </div>
            <div className="flex items-center gap-2 text-xs text-text-secondary">
              <span className="w-3 h-3 rounded bg-white/5 border border-border" /> Unanswered ({questions.length - answeredCount})
            </div>
            <div className="flex items-center gap-2 text-xs text-text-secondary">
              <span className="w-3 h-3 rounded bg-primary border border-primary/50" /> Current
            </div>
          </div>

          <button onClick={() => setShowConfirm(true)} className="btn-success w-full justify-center mt-4 py-2.5">
            Submit Test
          </button>
        </aside>
      </div>

      {/* Submit Confirmation Modal */}
      {showConfirm && (
        <div className="modal-overlay" onClick={() => setShowConfirm(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h3 className="text-lg font-semibold text-white mb-3">Submit Test?</h3>
            <p className="text-sm text-text-secondary mb-2">
              You have answered <span className="text-white font-semibold">{answeredCount}</span> out of <span className="text-white font-semibold">{questions.length}</span> questions.
            </p>
            {answeredCount < questions.length && (
              <p className="text-sm text-amber-400 mb-4">⚠️ {questions.length - answeredCount} questions are unanswered.</p>
            )}
            <div className="flex gap-2 mt-4">
              <button onClick={() => handleSubmit(false)} disabled={submitting} className="btn-success flex-1 justify-center py-2.5">
                {submitting ? <div className="spinner w-5 h-5 border-2" /> : 'Yes, Submit'}
              </button>
              <button onClick={() => setShowConfirm(false)} className="btn-outline flex-1 justify-center">Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
