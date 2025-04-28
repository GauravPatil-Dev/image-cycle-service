import React, { useEffect, useState, useRef } from "react";
import ReactDOM from "react-dom/client";

// Minimalist color palette
const COLORS = {
  bg: '#f8fafc',
  card: '#fff',
  border: '#e2e8f0',
  accent: '#2563eb',
  accentHover: '#1d4ed8',
  text: '#222',
  danger: '#ef4444',
  shadow: '0 2px 12px rgba(30,41,59,0.07)',
};

const API_URL = "http://localhost:8080/api/images";
const SSE_URL = `${API_URL}/stream`;

function App() {
  // Delete handler for images (optimistic update)
  // Optimistic UI delete: remove image from UI immediately
  // Better approach:
  // 1. Rollback on failure: If delete API fails, put the image back into images state.
  // 2. Refetch after failure: Optionally, re-fetch image list from backend to sync.
  // 3. Disable delete button while request is pending to avoid double deletes.
  // 4. Use local cache reconciliation (SWR/React Query) for robust optimistic updates.
  function handleDelete(id) {
    setImages(prev => {
      const next = prev.filter(img => img.id !== id);
      if (next.length === 0) setFrameIndices(Array(frameCount).fill(0));
      return next;
    });
    fetch(`${API_URL}/${id}`, { method: 'DELETE' })
      .then((res) => {
        if (res.ok) {
          document.dispatchEvent(new CustomEvent('toast', { detail: { message: 'Image deleted', type: 'success' } }));
        } else {
          // TODO: Rollback image in UI or refetch from backend here
          document.dispatchEvent(new CustomEvent('toast', { detail: { message: 'Failed to delete', type: 'error' } }));
        }
      })
      .catch(() => {
        // TODO: Rollback image in UI or refetch from backend here
        document.dispatchEvent(new CustomEvent('toast', { detail: { message: 'Failed to delete', type: 'error' } }));
      });
  }
  const [images, setImages] = useState([]);
  const [frameCount, setFrameCount] = useState(2); // default 2 frames
  const [frameIndices, setFrameIndices] = useState([0, 0]);
  const [toast, setToast] = useState(null); // { message, type }
  const intervalRef = useRef(null);

  // Show toast for 2 seconds
  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 2000);
  };

  // Listen for global toast events from delete handler
  useEffect(() => {
    const handler = (e) => {
      if (e.detail && e.detail.message) {
        setToast({ message: e.detail.message, type: e.detail.type });
        setTimeout(() => setToast(null), 2000);
      }
    };
    document.addEventListener('toast', handler);
    return () => document.removeEventListener('toast', handler);
  }, []);

  useEffect(() => {
    fetch(API_URL)
      .then((res) => res.json())
      .then((data) => {
        setImages(data);
      });
  }, []);

  useEffect(() => {
    const evtSource = new EventSource(SSE_URL);
    evtSource.onmessage = (e) => {
      let event;
      try {
        event = JSON.parse(e.data);
      } catch {
        return;
      }
      if (event && event.id && event.name && event.path) {
        setImages((prev) => {
          // If image already exists, skip
          if (prev.some((img) => img.id === event.id)) return prev;
          return [...prev, event];
        });
      } else if (typeof event === "string" && event.startsWith("deleted:")) {
        const id = event.replace("deleted:", "");
        setImages((prev) => {
          const next = prev.filter((img) => img.id !== id);
          // If no images left, reset frameIndices
          if (next.length === 0) setFrameIndices(Array(frameCount).fill(0));
          return next;
        });
      }
    };
    return () => evtSource.close();
  }, []);

  // Multi-frame cycling logic
  useEffect(() => {
    // Always keep indices in range after images or frameCount change
    if (intervalRef.current) clearInterval(intervalRef.current);
    if (images.length === 0) {
      setFrameIndices(Array(frameCount).fill(0));
      return;
    }
    setFrameIndices(prev => {
      // If frameCount increased, fill new indices with 0
      let next = prev.slice(0, frameCount);
      while (next.length < frameCount) next.push(0);
      // Clamp/wrap all indices
      return next.map(idx => idx % images.length);
    });
    intervalRef.current = setInterval(() => {
      setFrameIndices(prev => prev.map(idx => (images.length > 0 ? (idx + 1) % images.length : 0)));
    }, 2000);
    return () => clearInterval(intervalRef.current);
  }, [images, frameCount]);

  // Update frame count and indices when user changes number of frames
  const handleFrameCountChange = (e) => {
    let n = parseInt(e.target.value, 10);
    if (isNaN(n) || n < 1) n = 1;
    setFrameCount(n);
    setFrameIndices(Array(n).fill(0));
  };

  return (
    <div style={{ textAlign: "center", marginTop: 40 }}>
      <h2>Multi-Frame Image Viewer</h2>
      <div style={{ marginBottom: 20 }}>
        <label>
          Number of frames: 
          <input
            type="number"
            min={1}
            max={8}
            value={frameCount}
            onChange={handleFrameCountChange}
            style={{ width: 50, marginLeft: 8 }}
          />
        </label>
      </div>
      <div style={{ display: 'flex', justifyContent: 'center', gap: 24 }}>
        {Array.from({ length: frameCount }).map((_, i) => {
          if (images.length === 0) {
            return (
              <div
                key={i}
                style={{
                  textAlign: 'center',
                  background: COLORS.card,
                  borderRadius: 12,
                  boxShadow: COLORS.shadow,
                  border: `1px solid ${COLORS.border}`,
                  padding: 20,
                  minWidth: 220,
                  position: 'relative',
                  flex: 1,
                  maxWidth: 320,
                  color: COLORS.text,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  height: 240,
                }}
              >
                No images found.
              </div>
            );
          }
          // Defensive: show nothing if images empty or index out of bounds
          if (images.length === 0 || frameIndices[i] >= images.length) {
            return (
              <div
                key={i}
                style={{
                  textAlign: 'center',
                  background: COLORS.card,
                  borderRadius: 12,
                  boxShadow: COLORS.shadow,
                  border: `1px solid ${COLORS.border}`,
                  padding: 20,
                  minWidth: 220,
                  position: 'relative',
                  flex: 1,
                  maxWidth: 320,
                  color: COLORS.text,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  height: 240,
                }}
              >
                No images found.
              </div>
            );
          }
          // Defensive: if image is missing (stale index), show empty
          const img = images[frameIndices[i] % images.length];
          if (!img || !img.id) {
            return (
              <div
                key={i}
                style={{
                  textAlign: 'center',
                  background: COLORS.card,
                  borderRadius: 12,
                  boxShadow: COLORS.shadow,
                  border: `1px solid ${COLORS.border}`,
                  padding: 20,
                  minWidth: 220,
                  position: 'relative',
                  flex: 1,
                  maxWidth: 320,
                  color: COLORS.text,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  height: 240,
                }}
              >
                No images found.
              </div>
            );
          }
          return (
            <div
              key={i}
              style={{
                textAlign: 'center',
                background: COLORS.card,
                borderRadius: 12,
                boxShadow: COLORS.shadow,
                border: `1px solid ${COLORS.border}`,
                padding: 20,
                minWidth: 220,
                position: 'relative',
                flex: 1,
                maxWidth: 320,
              }}
            >
              <button
                aria-label="Delete image"
                title="Delete image"
                onClick={() => handleDelete(img.id)}
                style={{
                  position: 'absolute',
                  top: 10,
                  right: 10,
                  background: COLORS.danger,
                  border: 'none',
                  borderRadius: '50%',
                  width: 28,
                  height: 28,
                  color: '#fff',
                  fontWeight: 'bold',
                  cursor: 'pointer',
                  boxShadow: COLORS.shadow,
                  transition: 'background 0.2s',
                }}
              >
                ×
              </button>
              <img
                src={`${API_URL}/${img.id}/file`}
                alt={img.name}
                style={{
                  maxWidth: '100%',
                  maxHeight: 200,
                  borderRadius: 8,
                  border: `1px solid ${COLORS.border}`,
                  boxShadow: COLORS.shadow,
                  marginBottom: 10,
                  background: '#f1f5f9',
                  objectFit: 'cover',
                }}
              />
              <div style={{ fontSize: 15, color: COLORS.text, marginTop: 4 }}>
                Name: {img.name}
              </div>
            </div>
          );
        })}
      </div>
      <UploadForm showToast={showToast} />
      {/* Toast notification */}
      {toast && (
        <div
          style={{
            position: 'fixed',
            bottom: 30,
            left: '50%',
            transform: 'translateX(-50%)',
            background: toast.type === 'success' ? COLORS.accent : COLORS.danger,
            color: '#fff',
            padding: '12px 28px',
            borderRadius: 28,
            boxShadow: COLORS.shadow,
            fontSize: 16,
            zIndex: 1000,
            minWidth: 120,
            textAlign: 'center',
            letterSpacing: 0.2,
            opacity: 0.98,
          }}
        >
          {toast.message}
        </div>
      )}
    </div>
  );
}




function UploadForm({ showToast }) {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };
  const handleSubmit = (e) => {
    e.preventDefault();
    if (!file) return;
    setUploading(true);
    const formData = new FormData();
    formData.append("file", file);
    fetch("http://localhost:8080/api/images", {
      method: "POST",
      body: formData,
    })
      .then((res) => {
        setUploading(false);
        if (res.ok && showToast) showToast("Image uploaded");
        else if (showToast) showToast("Failed to upload", "error");
      })
      .catch(() => {
        setUploading(false);
        if (showToast) showToast("Failed to upload", "error");
      });
  };
  return (
    <form
      onSubmit={handleSubmit}
      style={{
        marginTop: 30,
        background: COLORS.card,
        borderRadius: 10,
        boxShadow: COLORS.shadow,
        padding: 16,
        display: 'inline-block',
        border: `1px solid ${COLORS.border}`,
      }}
    >
      <input
        type="file"
        accept="image/*"
        onChange={handleFileChange}
        style={{
          padding: 8,
          borderRadius: 6,
          border: `1px solid ${COLORS.border}`,
          background: '#f1f5f9',
        }}
      />
      <button
        type="submit"
        disabled={uploading || !file}
        style={{
          marginLeft: 12,
          padding: '8px 18px',
          background: uploading ? COLORS.accentHover : COLORS.accent,
          color: '#fff',
          border: 'none',
          borderRadius: 6,
          fontWeight: 500,
          boxShadow: COLORS.shadow,
          cursor: uploading || !file ? 'not-allowed' : 'pointer',
          transition: 'background 0.2s',
        }}
      >
        {uploading ? "Uploading..." : "Upload Image"}
      </button>
    </form>
  );
}



ReactDOM.createRoot(document.getElementById("root")).render(<App />);
