var jsonData, currentIndex = 0, qno = 1, seconds = 30, a, b, c, d, item, score = 0, total = 0;
  var shuffledOptions = [], timer;
  let fullScreenExitCount = 0;

  // Shuffle options
  function shuffleOptions() {
      shuffledOptions = ['A', 'B', 'C', 'D'].sort(() => Math.random() - 0.5);
  }

  // Check selected answer
  function checkAnswer() {
      if (a.checked && shuffledOptions[0] === item.correct) score++;
      else if (b.checked && shuffledOptions[1] === item.correct) score++;
      else if (c.checked && shuffledOptions[2] === item.correct) score++;
      else if (d.checked && shuffledOptions[3] === item.correct) score++;
  }

  // Display current question
  function displayCurrentRecord() {
      var lblqno = document.getElementById("lblqno");
      var lblquestion = document.getElementById("lblquestion");
      a = document.getElementById("a");
      b = document.getElementById("b");
      c = document.getElementById("c");
      d = document.getElementById("d");
      var lblA = document.getElementById("lblA");
      var lblB = document.getElementById("lblB");
      var lblC = document.getElementById("lblC");
      var lblD = document.getElementById("lblD");

      a.checked = b.checked = c.checked = d.checked = false;

      if (jsonData && jsonData.length > 0) {
          item = jsonData[currentIndex];
          lblqno.innerHTML = qno;
          lblquestion.innerHTML = item.question;

          shuffleOptions();
          lblA.innerHTML = item[shuffledOptions[0].toLowerCase()];
          lblB.innerHTML = item[shuffledOptions[1].toLowerCase()];
          lblC.innerHTML = item[shuffledOptions[2].toLowerCase()];
          lblD.innerHTML = item[shuffledOptions[3].toLowerCase()];
      }
  }

  // Timer
  function startTimer() {
      var timerDisplay = document.getElementById('timer');
      seconds = 30;
      timerDisplay.textContent = seconds;
      clearInterval(timer);
      timer = setInterval(function () {
          seconds--;
          timerDisplay.textContent = seconds;
          if (seconds <= 0) {
              clearInterval(timer);
              nextQuestion();
          }
      }, 1000);
  }

  // Start test
  function startTest() {
      jsonData = JSON.parse($("#jsonData").val());
      total = jsonData.length;
      displayCurrentRecord();
      startTimer();
      disableKeyboard();
  }

  // Next question
  function nextQuestion() {
      checkAnswer();
      currentIndex++;
      qno++;
      if (currentIndex === jsonData.length) {
          document.getElementById('totalInput').value = total;
          document.getElementById('scoreInput').value = score;
          document.getElementById('hiddenForm').submit();
      } else {
          displayCurrentRecord();
          startTimer();
      }
  }

  // Fullscreen mode
  function gofullscreen() {
      const element = document.documentElement;
      if (element.requestFullscreen) {
          element.requestFullscreen();
      } else if (element.webkitRequestFullscreen) {
          element.webkitRequestFullscreen();
      } else if (element.msRequestFullscreen) {
          element.msRequestFullscreen();
      } else {
          alert('Fullscreen mode is not supported by your browser.');
      }

      document.addEventListener('fullscreenchange', checkFullScreenExit);
      document.addEventListener('webkitfullscreenchange', checkFullScreenExit);
      document.addEventListener('msfullscreenchange', checkFullScreenExit);
  }

  // Exit fullscreen detection
  function checkFullScreenExit() {
      if (!document.fullscreenElement && !document.webkitIsFullScreen && !document.msFullscreenElement) {
          fullScreenExitCount++;
          if (fullScreenExitCount >= 2) {
              alert("You exited full screen twice. The test will now be submitted.");
              document.getElementById('totalInput').value = total;
              document.getElementById('scoreInput').value = score;
              document.getElementById('hiddenForm').submit();
          } else {
              // Show return modal instead of calling gofullscreen directly
              document.getElementById("fullscreenExitModal").style.display = 'flex';
          }
      }
  }

  
  // Disable keyboard
  function disableKeyboard() {
      document.addEventListener('keydown', function (e) {
          if (
              (e.ctrlKey || e.metaKey) && ['t', 'n', 'w','r','T', 'N', 'W','R'].includes(e.key.toLowerCase()) ||
              ['Escape', 'Alt'].includes(e.key)
          ) {
              e.preventDefault();
          }
      });

      document.addEventListener('contextmenu', function (e) {
          e.preventDefault();
      });

      window.addEventListener('beforeunload', function (e) {
          e.returnValue = '';
      });
  }

  // Modal logic
  document.addEventListener('DOMContentLoaded', () => {
      const modal = document.getElementById('modal');
      modal.style.display = 'flex';

      document.getElementById('closeModal').addEventListener('click', () => {
          modal.style.display = 'none';
          gofullscreen();
          startTest();
      });
  });

  // jQuery bindings
  $(document).ready(function () {
      $('#ButtonNext').on('click', nextQuestion);
  });