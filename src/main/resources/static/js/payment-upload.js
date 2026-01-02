/**
 * Payment Upload Page - JavaScript Handler
 * Menangani drag-drop, preview, dan upload bukti pembayaran.
 */
(function() {
  'use strict';

  // DOM Elements
  const dropzone = document.getElementById('dropzone');
  const fileInput = document.getElementById('paymentProof');
  const previewArea = document.getElementById('previewArea');
  const previewImage = document.getElementById('previewImage');
  const previewFile = document.getElementById('previewFile');
  const fileName = document.getElementById('fileName');
  const removeBtn = document.getElementById('removeFile');
  const submitBtn = document.getElementById('submitBtn');
  const uploadForm = document.getElementById('uploadForm');
  const successMessage = document.getElementById('successMessage');

  // Validasi: pastikan semua elemen ada
  if (!dropzone || !fileInput) {
    console.error('Required elements not found');
    return;
  }

  /**
   * Menangani file yang dipilih.
   * @param {File} file - File yang akan diproses
   */
  function handleFile(file) {
    if (!validateFile(file)) return;

    // Tampilkan preview
    showPreview(file);
    submitBtn.disabled = false;
  }

  /**
   * Memvalidasi file yang diunggah.
   * @param {File} file - File untuk divalidasi
   * @returns {boolean} - True jika valid
   */
  function validateFile(file) {
    const maxSize = 5 * 1024 * 1024; // 5MB
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf'];

    if (!file) {
      showToast('File tidak ditemukan', 'error');
      return false;
    }

    if (file.size > maxSize) {
      showToast('Ukuran file melebihi 5MB', 'error');
      return false;
    }

    if (!allowedTypes.includes(file.type)) {
      showToast('Format file tidak diizinkan', 'error');
      return false;
    }

    return true;
  }

  /**
   * Menampilkan preview file.
   * @param {File} file - File untuk di-preview
   */
  function showPreview(file) {
    previewArea.style.display = 'block';
    dropzone.style.display = 'none';

    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = function(e) {
        previewImage.src = e.target.result;
        previewImage.style.display = 'block';
        previewFile.style.display = 'none';
      };
      reader.readAsDataURL(file);
    } else {
      previewImage.style.display = 'none';
      previewFile.style.display = 'flex';
      fileName.textContent = file.name;
    }
  }

  /**
   * Reset form ke state awal.
   */
  function resetForm() {
    fileInput.value = '';
    previewArea.style.display = 'none';
    dropzone.style.display = 'flex';
    previewImage.src = '';
    submitBtn.disabled = true;
  }

  // Event: File input change
  fileInput.addEventListener('change', function(e) {
    if (e.target.files.length > 0) {
      handleFile(e.target.files[0]);
    }
  });

  // Event: Drag & Drop
  dropzone.addEventListener('dragover', function(e) {
    e.preventDefault();
    dropzone.classList.add('upload-dropzone--active');
  });

  dropzone.addEventListener('dragleave', function(e) {
    e.preventDefault();
    dropzone.classList.remove('upload-dropzone--active');
  });

  dropzone.addEventListener('drop', function(e) {
    e.preventDefault();
    dropzone.classList.remove('upload-dropzone--active');
    
    if (e.dataTransfer.files.length > 0) {
      handleFile(e.dataTransfer.files[0]);
      fileInput.files = e.dataTransfer.files;
    }
  });

  // Event: Remove file
  if (removeBtn) {
    removeBtn.addEventListener('click', resetForm);
  }

  // Event: Form submit
  uploadForm.addEventListener('submit', function(e) {
    e.preventDefault();

    const bankSelect = document.getElementById('bankSelect');
    
    if (!bankSelect.value || !fileInput.files.length) {
      showToast && showToast(!bankSelect.value ? 'Pilih bank terlebih dahulu' : 'Pilih file terlebih dahulu', 'error');
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Mengunggah...';

    const formData = new FormData();
    formData.append('paymentProof', fileInput.files[0]);
    formData.append('bank', bankSelect.value);

    fetch('/api/payment/upload', {
      method: 'POST',
      body: formData
    })
    .then(response => response.json())
    .then(data => {
      if (data.success) {
        // Tampilkan pesan sukses
        document.querySelector('.upload-card__body').innerHTML = successMessage.outerHTML;
        document.getElementById('successMessage').style.display = 'block';
        showToast('Bukti pembayaran berhasil diunggah!', 'success');
      } else {
        showToast(data.message || 'Gagal mengunggah', 'error');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Kirim Bukti Pembayaran';
      }
    })
    .catch(error => {
      console.error('Upload error:', error);
      showToast('Terjadi kesalahan saat mengunggah', 'error');
      submitBtn.disabled = false;
      submitBtn.textContent = 'Kirim Bukti Pembayaran';
    });
  });

})();
