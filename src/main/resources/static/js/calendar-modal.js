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

