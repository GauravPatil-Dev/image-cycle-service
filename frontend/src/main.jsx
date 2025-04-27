import React, { useEffect, useState, useRef } from "react";
import ReactDOM from "react-dom/client";

const API_URL = "http://localhost:8080/api/images";
const SSE_URL = `${API_URL}/stream`;

function App() {
  const [images, setImages] = useState([]);
  const [currentIdx, setCurrentIdx] = useState(0);
  const [imageName, setImageName] = useState("");
  const intervalRef = useRef(null);

  useEffect(() => {
    fetch(API_URL)
      .then((res) => res.json())
      .then((data) => {
        setImages(data);
        if (data.length > 0) setImageName(data[0].name);
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
        setImages((prev) => prev.filter((img) => img.id !== id));
      }
    };
    return () => evtSource.close();
  }, []);

  useEffect(() => {
    if (images.length === 0) return;
    setImageName(images[currentIdx]?.name || "");
    if (intervalRef.current) clearInterval(intervalRef.current);
    intervalRef.current = setInterval(() => {
      setCurrentIdx((idx) => (idx + 1) % images.length);
    }, 2000);
    return () => clearInterval(intervalRef.current);
  }, [images, currentIdx]);

  useEffect(() => {
    if (images.length > 0) setImageName(images[currentIdx]?.name || "");
  }, [currentIdx, images]);

  return (
    <div style={{ textAlign: "center", marginTop: 40 }}>
      <h2>Image Viewer</h2>
      {images.length > 0 ? (
        <>
          <img
            src={`${API_URL}/${images[currentIdx].id}/file`}
            alt={imageName}
            style={{ maxWidth: 400, maxHeight: 400, border: "1px solid #ccc" }}
          />
          <div style={{ marginTop: 10 }}>Name: {imageName}</div>
        </>
      ) : (
        <div>No images found.</div>
      )}
      <UploadForm />
    </div>
  );
}

function UploadForm() {
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
      .then(() => setUploading(false))
      .catch(() => setUploading(false));
  };
  return (
    <form onSubmit={handleSubmit} style={{ marginTop: 30 }}>
      <input type="file" accept="image/*" onChange={handleFileChange} />
      <button type="submit" disabled={uploading || !file} style={{ marginLeft: 8 }}>
        {uploading ? "Uploading..." : "Upload Image"}
      </button>
    </form>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);
