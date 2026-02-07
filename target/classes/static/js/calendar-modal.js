(function () {
  const dialog = document.getElementById('layout-dialog');
  if (!dialog) return;

  const calendar = document.querySelector('.calendar');
  if (!calendar) return;

  const sprite = document.querySelector('.assistant-sprite');
  if (sprite) {
    const cols = 4;
    const rows = 3;
    const sheetWidth = 1264;
    const sheetHeight = 848;
    const frameWidth = Math.round(sheetWidth / cols);
    const frameHeight = Math.round(sheetHeight / rows);
    const residualX = sheetWidth - frameWidth * cols;
    const residualY = sheetHeight - frameHeight * rows;
    const totalFrames = cols * rows - 1;
    const frameMs = 360;
    let frame = 0;

    sprite.style.backgroundPosition = '0px 0px';

    const prefersReduced = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (!prefersReduced) {
      setInterval(() => {
        const col = frame % cols;
        const row = Math.floor(frame / cols);
        const x = -col * frameWidth + (col === cols - 1 ? residualX : 0);
        const y = -row * frameHeight + (row === rows - 1 ? residualY : 0);
        sprite.style.backgroundPosition = `${x}px ${y}px`;
        frame = (frame + 1) % totalFrames;
      }, frameMs);
    }
  }

  const bubble = document.getElementById('assistantBubble');
  const bubbleMessage = bubble ? bubble.querySelector('.assistant-bubble-message') : null;
  const bubbleList = bubble ? bubble.querySelector('.assistant-bubble-list') : null;
  const bubbleText = {
    empty: (bubble && bubble.dataset.empty) || '日付を選択してください',
    none: (bubble && bubble.dataset.none) || 'この日のTODOはありません',
    loading: (bubble && bubble.dataset.loading) || '読み込み中...',
    loadError: (bubble && bubble.dataset.loadError) || '読み込みに失敗しました'
  };

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

  function animateBubble() {
    if (!bubble) return;
    bubble.classList.remove('is-updating');
    void bubble.offsetWidth;
    bubble.classList.add('is-updating');
  }

  function setBubbleMessage(text) {
    if (!bubbleMessage || !bubbleList) return;
    bubbleMessage.textContent = text;
    bubbleMessage.hidden = false;
    bubbleList.hidden = true;
    bubbleList.innerHTML = '';
    animateBubble();
  }

  function setBubbleTodos(data) {
    if (!bubbleMessage || !bubbleList) return;
    if (!data.length) {
      setBubbleMessage(bubbleText.none);
      return;
    }
    bubbleList.innerHTML = '';
    data.forEach((t) => {
      const li = document.createElement('li');
      li.textContent = t.title || '';
      bubbleList.appendChild(li);
    });
    bubbleMessage.hidden = true;
    bubbleList.hidden = false;
    animateBubble();
  }

  function closeDialog() {
    if (activeCell) {
      activeCell.classList.remove('is-open');
    }
    if (dialog.open) {
      dialog.close();
    }
    dialog.innerHTML = '';
    if (activeButton) {
      activeButton.focus();
    }
    activeCell = null;
    activeButton = null;
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
      if (typeof layout.transition === 'function') {
        layout.transition({ duration });
      }
    } else {
      dialog.showModal();
    }

    if (bubble) {
      setBubbleMessage(bubbleText.loading);
    }

    try {
      const res = await fetch(`/api/todos?date=${encodeURIComponent(date)}`);
      if (!res.ok) {
        detail.textContent = i18n.loadError;
        if (bubble) {
          setBubbleMessage(bubbleText.loadError);
        }
        return;
      }
      const json = await res.json();
      const data = json && json.data ? json.data : [];
      renderTodos(detail, data);
      if (bubble) {
        setBubbleTodos(data);
      }
    } catch (err) {
      detail.textContent = i18n.loadError;
      if (bubble) {
        setBubbleMessage(bubbleText.loadError);
      }
    }
  });
})();




