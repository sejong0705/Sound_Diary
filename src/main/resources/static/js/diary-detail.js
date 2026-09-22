document.addEventListener('DOMContentLoaded', () => {
  const editModeBtn = document.getElementById('editModeBtn');
  const saveBtn = document.getElementById('saveBtn');
  const cancelEditBtn = document.getElementById('cancelEditBtn');

  const fields = document.querySelectorAll('.sd-detail-field');
  const publicToggleWrap = document.getElementById('publicToggleWrap');
  const publicStatusText = document.getElementById('publicStatusText');

  const publicToggleUI = document.getElementById('publicToggleUI');
  const isPublicField = document.getElementById('isPublicField');

  // 취소 시 원래 값으로 되돌리기 위해 최초 값 저장
  const originalValues = {};
  fields.forEach(f => { originalValues[f.name] = f.value; });
  const originalIsPublic = isPublicField ? isPublicField.value : null;

  function enterEditMode() {
    fields.forEach(f => f.readOnly = false);
    if (fields[0]) fields[0].focus();

    if (publicToggleWrap) publicToggleWrap.classList.remove('d-none');
    if (publicStatusText) publicStatusText.classList.add('d-none');

    editModeBtn.classList.add('d-none');
    saveBtn.classList.remove('d-none');
    cancelEditBtn.classList.remove('d-none');
  }

  function exitEditMode() {
    fields.forEach(f => {
      f.readOnly = true;
      f.value = originalValues[f.name]; // 취소 시 원래 값 복원
    });

    if (isPublicField) isPublicField.value = originalIsPublic;
    if (publicToggleUI) publicToggleUI.checked = originalIsPublic === 'Y';

    if (publicToggleWrap) publicToggleWrap.classList.add('d-none');
    if (publicStatusText) publicStatusText.classList.remove('d-none');

    editModeBtn.classList.remove('d-none');
    saveBtn.classList.add('d-none');
    cancelEditBtn.classList.add('d-none');
  }

  if (editModeBtn) editModeBtn.addEventListener('click', enterEditMode);
  if (cancelEditBtn) cancelEditBtn.addEventListener('click', exitEditMode);

  if (publicToggleUI && isPublicField) {
    publicToggleUI.addEventListener('change', () => {
      isPublicField.value = publicToggleUI.checked ? 'Y' : 'N';
    });
  }

  // 삭제 (기존 로직 그대로)
  const deleteBtn = document.getElementById('deleteBtn');
  if (deleteBtn) {
    deleteBtn.addEventListener('click', async () => {
      if (!confirm('이 일기를 삭제할까요?')) return;
      const id = deleteBtn.dataset.id;
      try {
        const res = await fetch(`/api/diaries/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('delete failed');
        window.location.href = '/diary/list';
      } catch (e) {
        alert('삭제에 실패했어요.');
      }
    });
  }
});
const detailForm = document.getElementById('detailForm');

if (detailForm) {
  detailForm.addEventListener('submit', (e) => {
    const titleField = detailForm.querySelector('input[name="title"]');
    const contentField = detailForm.querySelector('textarea[name="content"]');

    if (!titleField.value.trim()) {
      e.preventDefault();
      alert('제목을 비워둘 수 없어요.');
      titleField.focus();
      return;
    }

    if (!contentField.value.trim()) {
      e.preventDefault();
      alert('내용을 비워둘 수 없어요.');
      contentField.focus();
      return;
    }
  });
}