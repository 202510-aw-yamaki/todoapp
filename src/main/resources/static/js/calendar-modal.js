(function () {
  const dialog = document.getElementById('layout-dialog');
  if (!dialog) return;

  const calendar = document.querySelector('.calendar');
  if (!calendar) return;

  const hasAnime = typeof window.anime !== 'undefined' && window.anime;
  if (!hasAnime) {
    console.warn('animejs UMD is not loaded. Place anime.umd.min.js under /static/lib/.');
  }

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

    detail.innerHTML = '<div>Loading...</div>';

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
        detail.textContent = 'Failed to load.';
        return;
      }
      const json = await res.json();
      const data = json && json.data ? json.data : [];
      if (!data.length) {
        detail.textContent = 'No todos.';
        return;
      }
      const ul = document.createElement('ul');
      data.forEach((t) => {
        const li = document.createElement('li');
        li.textContent = t.title || '';
        ul.appendChild(li);
      });
      detail.innerHTML = '';
      detail.appendChild(ul);
    } catch (err) {
      detail.textContent = 'Failed to load.';
    }
  });
})();
