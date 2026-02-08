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
    const title = document.getElementById("title").value;
    const folderId = document.getElementById("folderSelect").value;
    const file = document.getElementById("file").files[0];

    if (!title || !file) {
        alert("Title and file required");
        return;
    }

    const formData = new FormData();
    formData.append("title", title);
    formData.append("folderId", folderId);
    formData.append("file", file);

    fetch("/documents/upload", {
        method: "POST",
        body: formData
    }).then(() => {
        alert("Document uploaded");
        loadDocuments();
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
        tbody.innerHTML = `<tr><td colspan="3">No documents found</td></tr>`;
        return;
    }

    documents.forEach(doc => {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${doc.title}</td>
            <td>${doc.fileName}</td>
            <td>
                <button onclick="previewDoc(${doc.id})">Preview</button>
                <button onclick="downloadDoc(${doc.id})">Download</button>
                <button onclick="showVersions(${doc.id})">Versions</button>
                <button onclick="openVersionUpload(${doc.id})">Upload New Version</button>
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
