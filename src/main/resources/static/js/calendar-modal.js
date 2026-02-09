(function () {
  const dialog = document.getElementById('layout-dialog');
  if (!dialog) return;

  const calendar = document.querySelector('.calendar');
  if (!calendar) return;

  const hasAnime = typeof window.anime !== 'undefined' && window.anime;
  if (!hasAnime) {
    console.warn('animejs UMD is not loaded. Place anime.umd.min.js under /static/lib/.');
  }

  const i18n = {
    loading: dialog.dataset.loading || 'Loading... ',
    noTodos: dialog.dataset.noTodos || 'No todos.',
    loadError: dialog.dataset.loadError || 'Failed to load.',
    labelAuthor: dialog.dataset.labelAuthor || 'Author',
    labelTitle: dialog.dataset.labelTitle || 'Title',
    labelDetail: dialog.dataset.labelDetail || 'Detail',
    labelDeadline: dialog.dataset.labelDeadline || 'Deadline'
  };

  let activeCell = null;
  let activeButton = null;
  let frameTimer = null;
  let frameIndex = 0;
  let sidePanel = null;
  let frameImg = null;
  const frames = [
    '/images/assi/assi_01.png',
    '/images/assi/assi_02.png',
    '/images/assi/assi_03.png',
    '/images/assi/assi_04.png',
    '/images/assi/assi_05.png',
    '/images/assi/assi_06.png',
    '/images/assi/assi_07.png',
    '/images/assi/assi_08.png'
  ];
  preloadFrames();

  function closeDialog() {
    if (sidePanel) {
      sidePanel.classList.remove('is-open');
    }
    if (frameTimer) {
      clearInterval(frameTimer);
      frameTimer = null;
    }
    if (activeCell) {
      activeCell.classList.remove('is-open');
    }
    if (dialog.open) {
      dialog.close();
    }
    dialog.innerHTML = '';
    dialog.classList.remove('is-open');
    if (activeButton) {
      activeButton.focus();
    }
    activeCell = null;
    activeButton = null;
  }

  function ensureSidePanel() {
    if (sidePanel) {
      return;
    }
    sidePanel = document.createElement('div');
    sidePanel.className = 'modal-side';
    frameImg = document.createElement('img');
    frameImg.alt = 'assi animation';
    sidePanel.appendChild(frameImg);
    document.body.appendChild(sidePanel);
  }

  function preloadFrames() {
    frames.forEach((src) => {
      const img = new Image();
      img.src = src;
    });
  }

  function startFrameLoop(intervalMs) {
    ensureSidePanel();
    if (!frameImg) {
      return;
    }
    if (frameTimer) {
      clearInterval(frameTimer);
      frameTimer = null;
    }
    frameIndex = 0;
    frameImg.src = frames[frameIndex];
    sidePanel.classList.add('is-open');
    frameTimer = setInterval(() => {
      frameIndex = (frameIndex + 1) % frames.length;
      const next = frames[frameIndex];
      if (frameImg.decode) {
        frameImg.src = next;
        frameImg.decode().catch(() => {});
      } else {
        frameImg.src = next;
      }
    }, intervalMs);
  }

  dialog.addEventListener('click', (e) => {
    if (e.target === dialog) {
      closeDialog();
    }
  });

  dialog.addEventListener('cancel', (e) => {
    e.preventDefault();
    closeDialog();
  });

  function renderTodos(detail, data) {
    if (!data.length) {
      detail.textContent = i18n.noTodos;
      return;
    }
    detail.innerHTML = '';
    data.forEach((t) => {
      const item = document.createElement('div');
      item.className = 'todo-item';

      const author = document.createElement('div');
      author.className = 'todo-field';
      author.innerHTML = `<span class="todo-label">${i18n.labelAuthor}</span>${t.author || ''}`;

      const title = document.createElement('div');
      title.className = 'todo-field';
      title.innerHTML = `<span class="todo-label">${i18n.labelTitle}</span>${t.title || ''}`;

      const detailField = document.createElement('div');
      detailField.className = 'todo-field';
      detailField.innerHTML = `<span class="todo-label">${i18n.labelDetail}</span>${t.detail || ''}`;

      const deadline = document.createElement('div');
      deadline.className = 'todo-field';
      deadline.innerHTML = `<span class="todo-label">${i18n.labelDeadline}</span>${t.deadlineLabel || ''}`;

      item.appendChild(author);
      item.appendChild(title);
      item.appendChild(detailField);
      item.appendChild(deadline);
      detail.appendChild(item);
    });
  }

  calendar.addEventListener('click', async (e) => {
    const button = e.target.closest('.day-button');
    if (!button) return;
    const cell = button.closest('.item.day');
    if (!cell) return;

    activeCell = cell;
    activeButton = button;
    const date = cell.dataset.date;
    const duration = Number(cell.dataset.duration || 450);

    const clone = cell.cloneNode(true);
    const detail = clone.querySelector('.day-detail') || document.createElement('div');
    detail.classList.add('day-detail');

    if (!clone.querySelector('.day-detail')) {
      clone.appendChild(detail);
    }

    detail.innerHTML = `<div>${i18n.loading}</div>`;

    dialog.innerHTML = '';
    dialog.appendChild(clone);

    startFrameLoop(500);

    const closeTarget = clone.querySelector('.day-header');
    if (closeTarget) {
      closeTarget.addEventListener('click', (evt) => {
        evt.preventDefault();
        evt.stopPropagation();
        closeDialog();
      });
    }

    cell.classList.add('is-open');

    if (hasAnime && window.anime.createLayout) {
      const layout = window.anime.createLayout(dialog, {
        children: [clone],
        properties: ['--overlay-alpha']
      });
      layout.update(() => {
        dialog.showModal();
        dialog.classList.add('is-open');
      });
      try {
        if (window.anime && typeof window.anime.animate === 'function') {
          window.anime.animate({
            targets: clone,
            scale: [0.95, 1],
            opacity: [0, 1],
            duration,
            easing: 'easeOutCubic'
          });
        } else if (typeof window.anime === 'function') {
          window.anime({
            targets: clone,
            scale: [0.95, 1],
            opacity: [0, 1],
            duration,
            easing: 'easeOutCubic'
          });
        } else if (clone && typeof clone.animate === 'function') {
          clone.animate(
            [
              { transform: 'scale(0.95)', opacity: 0 },
              { transform: 'scale(1)', opacity: 1 }
            ],
            { duration, easing: 'cubic-bezier(0.2, 0.8, 0.2, 1)', fill: 'both' }
          );
        }
      } catch (e) {
        console.warn('open animation skipped:', e);
      }
      if (typeof layout.transition === 'function') {
        layout.transition({ duration });
      }
    } else {
      dialog.showModal();
    }

    try {
      const res = await fetch(`/api/todos?date=${encodeURIComponent(date)}`);
      if (!res.ok) {
        detail.textContent = i18n.loadError;
        return;
      }
      const json = await res.json();
      const data = json && json.data ? json.data : [];
      renderTodos(detail, data);
    } catch (err) {
      detail.textContent = i18n.loadError;
    }
  });
})();




