
    const state = {
      single: { file: null, init: null, etag: "" },
      multi: { file: null, init: null, chunkSize: 10 * 1024 * 1024, total: 0, done: 0, uploadedParts: new Set() }
    };

    const $ = (id) => document.getElementById(id);
    const logEl = $("log");

    function now() {
      const d = new Date();
      return d.toLocaleTimeString("en-GB", { hour12: false });
    }
    function log(msg, level = "INF") {
      logEl.textContent += `${now()} [${level}] ${msg}\n`;
      logEl.scrollTop = logEl.scrollHeight;
    }
    function status(id, text, type) {
      const el = $(id);
      el.textContent = text || "";
      el.className = `status ${type || "ok"}`;
    }
    function output(id, data) {
      $(id).textContent = typeof data === "string" ? data : JSON.stringify(data, null, 2);
    }
    function baseUrl() {
      const v = $("baseUrl").value.trim();
      return v ? v.replace(/\/+$/, "") : location.origin;
    }
    function headers(json = true) {
      const h = {};
      if (json) h["Content-Type"] = "application/json";
      const name = $("authHeaderName").value.trim();
      const token = $("authToken").value.trim();
      if (name && token) h[name] = token;
      return h;
    }
    function safeName(name) {
      return (name || "").split(/[\\/]/).pop();
    }
    function autoType(file, manual) {
      const m = (manual || "").trim();
      if (m) return m;
      if (file && file.type) return file.type;
      return "application/octet-stream";
    }
    function parseResult(raw) {
      let cur = raw;
      for (let i = 0; i < 6; i++) {
        if (!cur || typeof cur !== "object") return cur;
        if ("content" in cur && cur.content && typeof cur.content === "object") { cur = cur.content; continue; }
        if ("data" in cur && cur.data && typeof cur.data === "object") { cur = cur.data; continue; }
        if ("result" in cur && cur.result && typeof cur.result === "object") { cur = cur.result; continue; }
        if ("obj" in cur && cur.obj && typeof cur.obj === "object") { cur = cur.obj; continue; }
        if ("payload" in cur && cur.payload && typeof cur.payload === "object") { cur = cur.payload; continue; }
        if ("rows" in cur && cur.rows && typeof cur.rows === "object") { cur = cur.rows; continue; }
        break;
      }
      return cur;
    }
    function businessError(raw) {
      if (!raw || typeof raw !== "object") return "";
      const code = raw.code;
      const success = raw.success;
      const msg = raw.msg || raw.message || raw.error || "";
      if (typeof success === "boolean") {
        return success ? "" : (msg || `business error, success=${success}`);
      }
      if (code === undefined || code === null || code === "") return "";
      const pass = code === 200 || code === 0 || code === "200" || code === "0" || code === "SUCCESS" || code === "00000";
      return pass ? "" : (msg || `business error, code=${code}`);
    }
    function tryParseJson(text) {
      try { return JSON.parse(text); } catch (e) { return null; }
    }
    function pick(obj, keys) {
      if (!obj || typeof obj !== "object") return "";
      const lowerMap = {};
      Object.keys(obj).forEach(k => { lowerMap[k.toLowerCase()] = obj[k]; });
      for (const k of keys) {
        if (obj[k] !== undefined && obj[k] !== null && obj[k] !== "") return obj[k];
        const low = k.toLowerCase();
        if (lowerMap[low] !== undefined && lowerMap[low] !== null && lowerMap[low] !== "") return lowerMap[low];
      }
      return "";
    }
    function compactJson(obj) {
      try { return JSON.stringify(obj); } catch (e) { return String(obj); }
    }
    function normalizeSingleInit(init) {
      if (!init || typeof init !== "object") return null;
      const fileId = pick(init, ["fileId", "id", "file_id"]);
      const uploadId = pick(init, ["uploadId", "sessionId", "token", "upload_id", "session_id"]);
      const uploadUrl = pick(init, ["uploadUrl", "presignedUrl", "signedUrl", "url", "upload_url", "presigned_url"]);
      if (!fileId || !uploadId || !uploadUrl) return null;
      return Object.assign({}, init, { fileId, uploadId, uploadUrl });
    }
    function normalizeMultiInit(init) {
      if (!init || typeof init !== "object") return null;
      const fileId = pick(init, ["fileId", "id", "file_id"]);
      const uploadId = pick(init, ["uploadId", "sessionId", "token", "upload_id", "session_id"]);
      const totalParts = Number(pick(init, ["totalParts", "total_parts", "parts", "part_count"]) || 0);
      if (!fileId || !uploadId) return null;
      return Object.assign({}, init, { fileId, uploadId, totalParts: totalParts > 0 ? totalParts : undefined });
    }
    function fileMeta(file) {
      if (!file) return null;
      return { name: file.name, size: file.size, lastModified: file.lastModified };
    }
    function getProgressKeyByInit(init) {
      const uploadId = init && init.uploadId ? String(init.uploadId) : "";
      return uploadId ? `upload_test_multi_progress_${uploadId}` : "";
    }
    function toIntArray(arr) {
      if (!Array.isArray(arr)) return [];
      return arr.map(v => Number(v)).filter(v => Number.isInteger(v) && v > 0);
    }
    function saveMultiProgress() {
      if (!state.multi.init) return;
      const key = getProgressKeyByInit(state.multi.init);
      if (!key) return;
      const payload = {
        init: state.multi.init,
        uploadedParts: Array.from(state.multi.uploadedParts).sort((a, b) => a - b),
        total: state.multi.total,
        chunkSize: state.multi.chunkSize,
        fileMeta: fileMeta(state.multi.file),
        updatedAt: Date.now()
      };
      localStorage.setItem(key, JSON.stringify(payload));
      localStorage.setItem("upload_test_multi_last_progress_key", key);
    }
    function loadMultiProgressByInit(init) {
      const key = getProgressKeyByInit(init);
      if (!key) return null;
      return tryParseJson(localStorage.getItem(key) || "");
    }
    function loadLastMultiProgress() {
      const key = localStorage.getItem("upload_test_multi_last_progress_key") || "";
      if (!key) return null;
      return tryParseJson(localStorage.getItem(key) || "");
    }
    function restoreSingleState() {
      if (!state.single.file) {
        const f = $("singleFile").files && $("singleFile").files[0];
        if (f) state.single.file = f;
      }
      if (!state.single.init) {
        state.single.init = normalizeSingleInit(tryParseJson($("singleOut").textContent || ""));
      }
      if (!state.single.init) {
        state.single.init = normalizeSingleInit(tryParseJson(localStorage.getItem("upload_test_single_init") || ""));
      }
    }
    function restoreMultiState() {
      if (!state.multi.file) {
        const f = $("multiFile").files && $("multiFile").files[0];
        if (f) state.multi.file = f;
      }
      if (!state.multi.init) {
        state.multi.init = normalizeMultiInit(tryParseJson($("multiOut").textContent || ""));
      }
      if (!state.multi.init) {
        state.multi.init = normalizeMultiInit(tryParseJson(localStorage.getItem("upload_test_multi_init") || ""));
      }
      let progress = null;
      if (state.multi.init) progress = loadMultiProgressByInit(state.multi.init);
      if (!progress) progress = loadLastMultiProgress();
      if (progress && progress.init) {
        if (!state.multi.init) state.multi.init = normalizeMultiInit(progress.init);
        const uploaded = toIntArray(progress.uploadedParts);
        state.multi.uploadedParts = new Set(uploaded);
        state.multi.done = state.multi.uploadedParts.size;
        if (!state.multi.total && Number(progress.total) > 0) state.multi.total = Number(progress.total);
        if (Number(progress.chunkSize) > 0) state.multi.chunkSize = Number(progress.chunkSize);
      }
      if (state.multi.init && !state.multi.total && Number(state.multi.init.totalParts) > 0) {
        state.multi.total = Number(state.multi.init.totalParts);
      }
    }
    function isCrossOrigin(url) {
      try {
        return new URL(url).origin !== location.origin;
      } catch (e) {
        return false;
      }
    }

    async function post(path, payload) {
      const url = `${baseUrl()}${path}`;
      log(`POST ${path}`);
      const res = await fetch(url, {
        method: "POST",
        headers: headers(true),
        body: JSON.stringify(payload),
        credentials: "include"
      });
      const txt = await res.text();
      let json;
      try { json = txt ? JSON.parse(txt) : {}; } catch (e) { json = { raw: txt }; }
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${JSON.stringify(json)}`);
      }
      return json;
    }

    async function retry(fn, times, tag) {
      let last;
      for (let i = 0; i <= times; i++) {
        try { return await fn(); } catch (e) {
          last = e;
          if (i < times) {
            log(`${tag} retry ${i + 1}/${times}`, "WRN");
            await new Promise(r => setTimeout(r, 350 + 220 * i));
          }
        }
      }
      throw last;
    }

    // single
    $("singleFile").addEventListener("change", (e) => {
      state.single.file = e.target.files[0] || null;
      const f = state.single.file;
      if (f && !$("singleType").value.trim() && f.type) $("singleType").value = f.type;
      status("singleStatus", f ? `Selected: ${f.name} (${f.size} bytes)` : "", "ok");
    });

    async function singleInit() {
      const file = state.single.file;
      if (!file) throw new Error("Select a file first");
      const req = {
        originalName: safeName(file.name),
        fileSize: file.size,
        contentType: autoType(file, $("singleType").value),
        path: $("singlePath").value.trim(),
        fileMd5: $("singleMd5").value.trim()
      };
      const raw = await post("/open-api/file/presigned-url", req);
      const be = businessError(raw);
      if (be) {
        output("singleOut", raw);
        throw new Error(`Init business error: ${be}`);
      }
      const data = normalizeSingleInit(parseResult(raw));
      if (!data) {
        output("singleOut", raw);
        throw new Error(`Init response missing fileId/uploadId/uploadUrl. raw=${compactJson(raw)}`);
      }
      state.single.init = data;
      localStorage.setItem("upload_test_single_init", JSON.stringify(data));
      output("singleOut", data);
      status("singleStatus", "Init success", "ok");
      log(`single init done, fileId=${data && data.fileId}`);
      return data;
    }

    async function singlePut() {
      restoreSingleState();
      const file = state.single.file;
      const init = state.single.init;
      if (!file) throw new Error("File missing, reselect file");
      if (!init) throw new Error("Init not found, click Init again");
      if (!init.uploadUrl) throw new Error("Init has no uploadUrl");
      status("singleStatus", "Uploading by PUT...", "warn");
      try {
        const res = await fetch(init.uploadUrl, { method: "PUT", body: file });
        if (!res.ok) {
          const txt = await res.text();
          throw new Error(`PUT failed ${res.status}: ${txt}`);
        }
        state.single.etag = (res.headers.get("ETag") || "").replace(/"/g, "");
      } catch (e) {
        if (String(e).includes("Failed to fetch") && isCrossOrigin(init.uploadUrl)) {
          throw new Error("PUT blocked by CORS. Please configure MinIO bucket CORS for this origin.");
        }
        throw e;
      }
      status("singleStatus", "PUT success", "ok");
      log("single put done");
    }

    async function singleCallback() {
      restoreSingleState();
      const init = state.single.init;
      if (!init) throw new Error("Init not found, click Init again");
      const req = {
        fileId: init.fileId,
        uploadId: init.uploadId,
        fileMd5: $("singleMd5").value.trim(),
        etag: state.single.etag || undefined
      };
      const raw = await post("/open-api/file/upload-callback", req);
      const data = parseResult(raw);
      output("singleOut", data);
      status("singleStatus", `Callback done, fileStatus=${data && data.fileStatus}`, "ok");
      log(`single callback done, parseTriggered=${data && data.parseTriggered}`);
      return data;
    }

    $("singleInitBtn").addEventListener("click", async () => {
      try { await singleInit(); } catch (e) { status("singleStatus", e.message, "err"); log(e.message, "ERR"); }
    });
    $("singlePutBtn").addEventListener("click", async () => {
      try { await singlePut(); } catch (e) { status("singleStatus", e.message, "err"); log(e.message, "ERR"); }
    });
    $("singleCbBtn").addEventListener("click", async () => {
      try { await singleCallback(); } catch (e) { status("singleStatus", e.message, "err"); log(e.message, "ERR"); }
    });
    $("singleAllBtn").addEventListener("click", async () => {
      try { await singleInit(); await singlePut(); await singleCallback(); } catch (e) {
        status("singleStatus", e.message, "err");
        log(e.message, "ERR");
      }
    });

    // multipart
    $("multiFile").addEventListener("change", (e) => {
      state.multi.file = e.target.files[0] || null;
      const f = state.multi.file;
      if (f && !$("multiType").value.trim() && f.type) $("multiType").value = f.type;
      status("multiStatus", f ? `Selected: ${f.name} (${f.size} bytes)` : "", "ok");
    });
    function chunkSize() {
      const mb = Number($("chunkMb").value || 10);
      if (!Number.isFinite(mb) || mb <= 0) throw new Error("chunk MB must be > 0");
      return Math.floor(mb * 1024 * 1024);
    }
    function parallel() {
      const n = Number($("parallelNum").value || 3);
      if (!Number.isFinite(n) || n <= 0) throw new Error("parallel must be > 0");
      return Math.min(8, Math.floor(n));
    }
    function breakAfterParts() {
      const n = Number($("breakAfterParts").value || 0);
      if (!Number.isFinite(n) || n < 0) return 0;
      return Math.floor(n);
    }
    function getPendingParts() {
      const pending = [];
      for (let i = 1; i <= state.multi.total; i++) {
        if (!state.multi.uploadedParts.has(i)) pending.push(i);
      }
      return pending;
    }
    function ensureFileMatchesProgress(progressObj) {
      if (!progressObj || !progressObj.fileMeta || !state.multi.file) return;
      const a = progressObj.fileMeta;
      const b = fileMeta(state.multi.file);
      if (!b) return;
      if (a.name !== b.name || a.size !== b.size || a.lastModified !== b.lastModified) {
        throw new Error("Selected file does not match cached progress file. Please reselect the same file.");
      }
    }

    async function multiInit() {
      const file = state.multi.file;
      if (!file) throw new Error("Select a file first");
      state.multi.chunkSize = chunkSize();
      state.multi.total = Math.ceil(file.size / state.multi.chunkSize);
      state.multi.done = 0;
      state.multi.uploadedParts = new Set();
      const req = {
        originalName: safeName(file.name),
        fileSize: file.size,
        totalParts: state.multi.total,
        contentType: autoType(file, $("multiType").value),
        path: $("multiPath").value.trim(),
        fileMd5: $("multiMd5").value.trim()
      };
      const raw = await post("/open-api/file/multipart/init", req);
      const be = businessError(raw);
      if (be) {
        output("multiOut", raw);
        throw new Error(`Init business error: ${be}`);
      }
      const data = normalizeMultiInit(parseResult(raw));
      if (!data) {
        output("multiOut", raw);
        throw new Error(`Init response missing fileId/uploadId. raw=${compactJson(raw)}`);
      }
      state.multi.init = data;
      state.multi.init.totalParts = state.multi.total;
      localStorage.setItem("upload_test_multi_init", JSON.stringify(data));
      saveMultiProgress();
      output("multiOut", data);
      status("multiStatus", `Init success, totalParts=${state.multi.total}`, "ok");
      log(`multi init done, fileId=${data && data.fileId}`);
      return data;
    }

    async function uploadPart(partNumber) {
      restoreMultiState();
      const file = state.multi.file;
      const init = state.multi.init;
      if (!file) throw new Error("File missing, reselect file");
      if (!init) throw new Error("Init not found, click Init again");
      if (state.multi.uploadedParts.has(partNumber)) {
        return;
      }
      const start = (partNumber - 1) * state.multi.chunkSize;
      const end = Math.min(file.size, start + state.multi.chunkSize);
      const blob = file.slice(start, end);

      const signReq = { fileId: init.fileId, uploadId: init.uploadId, partNumber };
      log(`sign payload: ${JSON.stringify(signReq)}`);
      const signRaw = await retry(() => post("/open-api/file/multipart/sign", signReq), 2, `sign p${partNumber}`);
      const signData = parseResult(signRaw);
      if (!signData || !pick(signData, ["uploadUrl", "presignedUrl", "url"])) {
        throw new Error(`sign response has no uploadUrl for part ${partNumber}`);
      }
      const partUploadUrl = pick(signData, ["uploadUrl", "presignedUrl", "url"]);

      await retry(async () => {
        try {
          const res = await fetch(partUploadUrl, { method: "PUT", body: blob });
          if (!res.ok) {
            const txt = await res.text();
            throw new Error(`part ${partNumber} PUT failed ${res.status}: ${txt}`);
          }
        } catch (e) {
          if (String(e).includes("Failed to fetch") && isCrossOrigin(partUploadUrl)) {
            throw new Error(`part ${partNumber} blocked by CORS on MinIO`);
          }
          throw e;
        }
      }, 2, `upload p${partNumber}`);

      state.multi.done += 1;
      state.multi.uploadedParts.add(partNumber);
      saveMultiProgress();
      status("multiStatus", `Uploading parts... ${state.multi.done}/${state.multi.total}`, "warn");
      log(`part ${partNumber} uploaded`);
    }

    async function runPool(tasks, limit) {
      const workers = Array(limit).fill(0).map(async () => {
        while (tasks.length) {
          const fn = tasks.shift();
          if (!fn) return;
          await fn();
        }
      });
      await Promise.all(workers);
    }

    async function multiUploadAll() {
      restoreMultiState();
      const file = state.multi.file;
      const init = state.multi.init;
      if (!file || !init) throw new Error("Run init first");
      ensureFileMatchesProgress(loadMultiProgressByInit(init) || loadLastMultiProgress());
      if (!state.multi.total) throw new Error("Total parts unknown. Please init again.");
      const pending = getPendingParts();
      state.multi.done = state.multi.uploadedParts.size;
      if (pending.length === 0) {
        status("multiStatus", `No pending parts. ${state.multi.total}/${state.multi.total}`, "ok");
        return;
      }
      const breakN = breakAfterParts();
      if (breakN > 0) {
        const subset = pending.slice(0, breakN);
        status("multiStatus", `Uploading ${subset.length} parts then simulate interrupt`, "warn");
        for (const partNo of subset) {
          await uploadPart(partNo);
        }
        status("multiStatus", `Simulated interrupt. Uploaded this run=${subset.length}, pending=${getPendingParts().length}`, "warn");
        log(`simulated break after ${subset.length} parts`);
        return;
      }

      const list = pending.map(i => () => uploadPart(i));
      const p = parallel();
      status("multiStatus", `Start uploading pending parts=${pending.length}, parallel=${p}`, "warn");
      await runPool(list, p);
      status("multiStatus", `All pending parts uploaded. ${state.multi.uploadedParts.size}/${state.multi.total}`, "ok");
      log("all pending parts uploaded");
    }

    async function multiComplete() {
      restoreMultiState();
      const init = state.multi.init;
      if (!init) throw new Error("Run init first");
      if (state.multi.total > 0) {
        const pending = getPendingParts();
        if (pending.length > 0) {
          throw new Error(`Cannot complete. Pending parts=${pending.length}`);
        }
      }
      const raw = await post("/open-api/file/multipart/complete", {
        fileId: init.fileId,
        uploadId: init.uploadId
      });
      const data = parseResult(raw);
      output("multiOut", data);
      status("multiStatus", `Complete success, fileStatus=${data && data.fileStatus}`, "ok");
      log(`multipart complete done, parseTriggered=${data && data.parseTriggered}`);
      return data;
    }

    $("multiInitBtn").addEventListener("click", async () => {
      try { await multiInit(); } catch (e) { status("multiStatus", e.message, "err"); log(e.message, "ERR"); }
    });
    $("multiUploadBtn").addEventListener("click", async () => {
      try { await multiUploadAll(); } catch (e) { status("multiStatus", e.message, "err"); log(e.message, "ERR"); }
    });
    $("multiResumeBtn").addEventListener("click", async () => {
      try {
        $("breakAfterParts").value = "0";
        await multiUploadAll();
      } catch (e) {
        status("multiStatus", e.message, "err");
        log(e.message, "ERR");
      }
    });
    $("multiDoneBtn").addEventListener("click", async () => {
      try { await multiComplete(); } catch (e) { status("multiStatus", e.message, "err"); log(e.message, "ERR"); }
    });
    $("multiClearCacheBtn").addEventListener("click", () => {
      try {
        const init = state.multi.init || normalizeMultiInit(tryParseJson(localStorage.getItem("upload_test_multi_init") || ""));
        if (init) {
          const key = getProgressKeyByInit(init);
          if (key) localStorage.removeItem(key);
        }
        localStorage.removeItem("upload_test_multi_last_progress_key");
        localStorage.removeItem("upload_test_multi_init");
        state.multi.uploadedParts = new Set();
        status("multiStatus", "Resume cache cleared", "ok");
        log("multi resume cache cleared");
      } catch (e) {
        status("multiStatus", e.message, "err");
        log(e.message, "ERR");
      }
    });
    $("multiAllBtn").addEventListener("click", async () => {
      try {
        $("breakAfterParts").value = "0";
        await multiInit();
        await multiUploadAll();
        await multiComplete();
      } catch (e) {
        status("multiStatus", e.message, "err");
        log(e.message, "ERR");
      }
    });

    $("clearLogBtn").addEventListener("click", () => { logEl.textContent = ""; });
    $("baseUrl").value = location.origin;
    log("page ready");
  
