let currentDocumentId = null;

window.onload = function () {
    loadFolders();
};

/* =========================
   LOAD FOLDERS
========================= */
function loadFolders() {
    fetch("/folders")
        .then(res => res.json())
        .then(folders => {
            const folderSelect = document.getElementById("folderSelect");
            const viewFolderSelect = document.getElementById("viewFolderSelect");

            folderSelect.innerHTML = "";
            viewFolderSelect.innerHTML = "";

            folders.forEach(f => {
                const opt1 = new Option(f.name, f.id);
                const opt2 = new Option(f.name, f.id);
                folderSelect.add(opt1);
                viewFolderSelect.add(opt2);
            });

            // 🔥 auto load documents for first folder
            if (folders.length > 0) {
                loadDocuments();
            }
        });
}

/* =========================
   CREATE FOLDER
========================= */
function createFolder() {
    const name = document.getElementById("folderName").value;

    if (!name) {
        alert("Enter folder name");
        return;
    }

    fetch("/folders", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name })
    }).then(() => {
        alert("Folder created");
        loadFolders();
    });
}

/* =========================
   UPLOAD DOCUMENT (v1)
========================= */
function uploadDocument() {

    const title = document.getElementById("title").value.trim();
    const tags = document.getElementById("tags").value;
    const folderId = document.getElementById("folderSelect").value;
    const fileInput = document.getElementById("file");
    const file = fileInput.files[0];

    if (!title) {
        alert("Document title is required");
        return;
    }

    if (!file) {
        alert("Please select a file");
        return;
    }

    // File size validation (10MB)
    if (file.size > 10 * 1024 * 1024) {
        alert("File size must be less than 10MB");
        return;
    }

    // File type validation
    const allowedTypes = [
        "application/pdf",
        "text/plain",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    ];

    if (!allowedTypes.includes(file.type)) {
        alert("Only PDF, DOCX, and TXT files are allowed");
        return;
    }

    const formData = new FormData();
    formData.append("title", title);
    formData.append("tags", tags);
    formData.append("folderId", folderId);
    formData.append("file", file);

    fetch("/documents/upload", {
        method: "POST",
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Upload failed");
        }
        return response.text();
    })
    .then(data => {
        alert("Document uploaded successfully");
        document.getElementById("title").value = "";
        document.getElementById("tags").value = "";
        fileInput.value = "";
        loadDocuments();
    })
    .catch(error => {
        console.error(error);
        alert("Error uploading document");
    });
}

/* =========================
   LOAD DOCUMENTS
========================= */
function loadDocuments() {
    const folderId = document.getElementById("viewFolderSelect").value;

    fetch(`/documents/folders/${folderId}/documents`)
        .then(res => res.json())
        .then(data => {
            console.log("Documents:", data); // debug
            renderTable(data);
        });
}

/* =========================
   SEARCH DOCUMENTS
========================= */
function searchDocuments() {
    const title = document.getElementById("searchInput").value;

    fetch(`/documents/search?title=${title}`)
        .then(res => res.json())
        .then(renderTable);
}

/* =========================
   RENDER DOCUMENT TABLE
========================= */
function renderTable(documents) {
    const tbody = document.getElementById("docTable");
    tbody.innerHTML = "";

    if (documents.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4">No documents found</td></tr>`;
        return;
    }

    documents.forEach(doc => {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${doc.title}</td>
            <td>${doc.fileName}</td>
            <td>${doc.owner?.name || "N/A"}</td>
            <td>
                <button onclick="previewDoc(${doc.id})">Preview</button>
                <button onclick="downloadDoc(${doc.id})">Download</button>
                <button onclick="showVersions(${doc.id})">Versions</button>
                <button onclick="openVersionUpload(${doc.id})">Upload New Version</button>
                <button onclick="openAIModal(${doc.id})">Ask AI</button>
            </td>
        `;

        tbody.appendChild(row);
    });
}


/* =========================
   PREVIEW & DOWNLOAD
========================= */
function previewDoc(id) {
    window.open(`/documents/${id}/preview`, "_blank");
}

function downloadDoc(id) {
    window.location.href = `/documents/${id}/download`;
}

/* =========================
   VERSION HISTORY
========================= */
function showVersions(documentId) {
    fetch(`/documents/${documentId}/versions`)
        .then(res => res.json())
        .then(versions => {
            const tbody = document.getElementById("versionTable");
            tbody.innerHTML = "";

            versions.forEach(v => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>v${v.versionNumber}</td>
                    <td>${v.uploadedAt}</td>
                    <td>${v.current ? "✅" : "❌"}</td>
                    <td>
                        <button onclick="downloadVersion(${v.id})">Download</button>
                    </td>
                `;
                tbody.appendChild(row);
            });

            document.getElementById("versionModal").style.display = "block";
        });
}

function downloadVersion(versionId) {
    window.location.href = `/documents/versions/${versionId}/download`;
}

function closeModal() {
    document.getElementById("versionModal").style.display = "none";
}

/* =========================
   UPLOAD NEW VERSION (v2, v3…)
========================= */
function openVersionUpload(documentId) {
    currentDocumentId = documentId;
    document.getElementById("uploadVersionModal").style.display = "block";
}

function closeUploadVersion() {
    document.getElementById("uploadVersionModal").style.display = "none";
    document.getElementById("versionFile").value = "";
}

function uploadNewVersion() {
    const file = document.getElementById("versionFile").files[0];

    if (!file) {
        alert("Select a file");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    fetch(`/documents/${currentDocumentId}/upload-version`, {
        method: "POST",
        body: formData
    }).then(() => {
        alert("New version uploaded (v2+)");
        closeUploadVersion();
        showVersions(currentDocumentId);
        loadDocuments();
    });
}

/* =========================
   AI ASK FEATURE
========================= */

function openAIModal(documentId) {
    currentDocumentId = documentId;
    document.getElementById("aiModal").style.display = "block";
}

function closeAIModal() {
    document.getElementById("aiModal").style.display = "none";
    document.getElementById("aiQuestion").value = "";
    document.getElementById("aiResult").innerText = "";
}

function askAI() {

    const question = document.getElementById("aiQuestion").value;

    if (!question) {
        alert("Enter a question");
        return;
    }

    fetch(`/api/ai/ask/${currentDocumentId}`, {
        method: "POST",
        headers: {
            "Content-Type": "text/plain"
        },
        body: question
    })
    .then(res => res.text())
    .then(answer => {
        document.getElementById("aiResult").innerText = answer;
    })
    .catch(err => {
        console.error(err);
        document.getElementById("aiResult").innerText = "Error calling AI";
    });
}