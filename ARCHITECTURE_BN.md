# মাল্টি-ফাইল আপলোড সিস্টেম - আর্কিটেকচার ডকুমেন্টেশন

## সারসংক্ষেপ

এই প্রজেক্টটি একটি Spring Boot অ্যাপ্লিকেশন যেখানে ব্যবহারকারী একসাথে একাধিক ফাইল আপলোড করতে পারবেন। ফাইলগুলো তাদের টাইপ অনুযায়ী স্বয়ংক্রিয়ভাবে সঠিক ফোল্ডারে সংরক্ষিত হবে:

| ফাইল টাইপ | এক্সটেনশন | ফোল্ডার |
|-----------|-----------|---------|
| ছবি | .jpg, .png, .gif, .webp, .svg, .ico, .bmp | `static/images/` |
| CSS | .css | `static/css/` |
| HTML | .html, .htm | `static/html/` |

---

## প্রজেক্ট স্ট্রাকচার

```
src/main/
├── java/com/codemania/templateupload/
│   ├── TemplateUploadApplication.java    ← অ্যাপ্লিকেশন এন্ট্রি পয়েন্ট
│   ├── controller/
│   │   └── FileUploadController.java     ← HTTP রিকোয়েস্ট হ্যান্ডলার
│   ├── service/
│   │   └── FileUploadService.java        ← বিজনেস লজিক
│   └── dto/
│       └── FileUploadResult.java         ← ডেটা ট্রান্সফার অবজেক্ট
└── resources/
    ├── application.properties            ← কনফিগারেশন
    ├── static/
    │   ├── images/                       ← ছবি আপলোড ফোল্ডার
    │   ├── css/                          ← CSS আপলোড ফোল্ডার
    │   └── html/                         ← HTML আপলোড ফোল্ডার
    └── templates/
        └── upload.html                   ← Thymeleaf টেমপ্লেট
```

---

## আর্কিটেকচার ডায়াগ্রাম

```
┌─────────────────────────────────────────────────────────────────┐
│                        ব্যবহারকারী                                │
│                    (ওয়েব ব্রাউজার)                               │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                    upload.html (View)                           │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  <form enctype="multipart/form-data">                   │   │
│  │    <input type="file" multiple name="files">            │   │
│  │    <button>Upload</button>                              │   │
│  │  </form>                                                │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────┬───────────────────────────────────────┘
                          │ POST /upload
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│              FileUploadController (Controller)                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  @PostMapping("/upload")                                │   │
│  │  handleFileUpload(MultipartFile[] files)                │   │
│  │     ↓                                                   │   │
│  │  fileUploadService.saveFiles(files)                     │   │
│  │     ↓                                                   │   │
│  │  redirectAttributes.addFlashAttribute("results", ...)   │   │
│  │     ↓                                                   │   │
│  │  return "redirect:/"                                    │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│               FileUploadService (Service)                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  saveFiles(MultipartFile[] files)                       │   │
│  │     ↓                                                   │   │
│  │  for each file:                                         │   │
│  │    1. sanitizeFilename() → নিরাপদ ফাইলনাম তৈরি          │   │
│  │    2. getFileExtension() → এক্সটেনশন বের করা            │   │
│  │    3. EXTENSION_MAP.get() → ফাইল টাইপ নির্ধারণ          │   │
│  │    4. FOLDER_MAP.get() → টার্গেট ফোল্ডার নির্বাচন        │   │
│  │    5. Files.copy() → ফাইল সংরক্ষণ                       │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ফাইল সিস্টেম                                  │
│  ┌──────────────┬──────────────┬──────────────┐                │
│  │   images/    │     css/     │    html/     │                │
│  │  photo.jpg   │  style.css   │  index.html  │                │
│  │  logo.png    │  theme.css   │  about.htm   │                │
│  └──────────────┴──────────────┴──────────────┘                │
└─────────────────────────────────────────────────────────────────┘
```

---

## কোড ব্যাখ্যা

### ১. FileUploadResult.java (DTO)

```java
public record FileUploadResult(
        String filename,      // ফাইলের নাম
        String savedPath,     // যেখানে সেভ হয়েছে (যেমন: images/photo.jpg)
        FileType fileType,    // IMAGE, CSS, HTML, UNKNOWN
        boolean success,      // সফল হয়েছে কিনা
        String message        // মেসেজ (সফল/ব্যর্থের কারণ)
) { }
```

**কেন record ব্যবহার করা হয়েছে?**
- Java 16+ এ `record` হলো immutable ডেটা ক্লাস তৈরির সহজ উপায়
- স্বয়ংক্রিয়ভাবে constructor, getter, `equals()`, `hashCode()`, `toString()` তৈরি হয়
- কোড কম লিখতে হয়, বাগের সম্ভাবনা কম

**Static Factory Methods:**
```java
// সফল আপলোডের জন্য
FileUploadResult.success("photo.jpg", "images/photo.jpg", FileType.IMAGE);

// ব্যর্থ আপলোডের জন্য
FileUploadResult.failure("virus.exe", "File type not allowed");
```

---

### ২. FileUploadService.java (Service Layer)

#### এক্সটেনশন ম্যাপিং

```java
private static final Map<String, FileType> EXTENSION_MAP = Map.ofEntries(
    // ছবি
    Map.entry("jpg", FileType.IMAGE),
    Map.entry("jpeg", FileType.IMAGE),
    Map.entry("png", FileType.IMAGE),
    // ...
    // CSS
    Map.entry("css", FileType.CSS),
    // HTML
    Map.entry("html", FileType.HTML),
    Map.entry("htm", FileType.HTML)
);
```

**কেন Map ব্যবহার?**
- O(1) time complexity - তাৎক্ষণিক lookup
- if-else চেইনের চেয়ে অনেক দ্রুত এবং পরিষ্কার

#### ফোল্ডার ম্যাপিং

```java
private static final Map<FileType, String> FOLDER_MAP = Map.of(
    FileType.IMAGE, "images",
    FileType.CSS, "css",
    FileType.HTML, "html"
);
```

#### মূল সেভ লজিক

```java
public FileUploadResult saveFile(MultipartFile file) {
    // ১. ফাইলনাম স্যানিটাইজ (বিশেষ অক্ষর সরানো)
    String sanitizedFilename = sanitizeFilename(originalFilename);

    // ২. এক্সটেনশন বের করা
    String extension = getFileExtension(sanitizedFilename).toLowerCase();

    // ৩. অনুমোদিত কিনা চেক
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
        return FileUploadResult.failure(sanitizedFilename, "File type not allowed");
    }

    // ৪. ফাইল টাইপ এবং ফোল্ডার নির্ধারণ
    FileType fileType = EXTENSION_MAP.get(extension);
    String targetFolder = FOLDER_MAP.get(fileType);

    // ৫. ফোল্ডার তৈরি (না থাকলে)
    Files.createDirectories(targetDirectory);

    // ৬. ফাইল সেভ (আগে থাকলে overwrite)
    Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

    return FileUploadResult.success(sanitizedFilename, savedPath, fileType);
}
```

#### ফাইলনাম স্যানিটাইজেশন

```java
private String sanitizeFilename(String filename) {
    return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
}
```

**কেন এটা গুরুত্বপূর্ণ?**
- `../../../etc/passwd` এর মতো path traversal attack প্রতিরোধ করে
- স্পেশাল ক্যারেক্টার আন্ডারস্কোর (`_`) দিয়ে প্রতিস্থাপিত হয়
- উদাহরণ: `my photo (1).jpg` → `my_photo__1_.jpg`

---

### ৩. FileUploadController.java (Controller Layer)

#### GET রিকোয়েস্ট - পেজ দেখানো

```java
@GetMapping("/")
public String showUploadForm(Model model) {
    return "upload";  // templates/upload.html রেন্ডার করবে
}
```

#### POST রিকোয়েস্ট - ফাইল আপলোড

```java
@PostMapping("/upload")
public String handleFileUpload(
        @RequestParam("files") MultipartFile[] files,
        RedirectAttributes redirectAttributes
) {
    // ১. ফাইল চেক
    if (files == null || files.length == 0) {
        redirectAttributes.addFlashAttribute("error", "Please select files");
        return "redirect:/";
    }

    // ২. সার্ভিস কল
    List<FileUploadResult> results = fileUploadService.saveFiles(files);

    // ৩. রেজাল্ট পাস
    redirectAttributes.addFlashAttribute("results", results);

    // ৪. রিডাইরেক্ট (PRG প্যাটার্ন)
    return "redirect:/";
}
```

**Post-Redirect-Get (PRG) প্যাটার্ন কেন?**

```
সমস্যা: POST → পেজ → ব্রাউজার রিফ্রেশ → আবার POST! (ডুপ্লিকেট আপলোড)

সমাধান (PRG):
POST → Redirect → GET → পেজ
                     ↓
           রিফ্রেশ করলে শুধু GET হবে (নিরাপদ)
```

**Flash Attributes কিভাবে কাজ করে?**

```java
redirectAttributes.addFlashAttribute("results", results);
```

- Session এ অস্থায়ীভাবে ডেটা রাখে
- পরবর্তী রিকোয়েস্টে Model এ স্বয়ংক্রিয়ভাবে যুক্ত হয়
- একবার পড়ার পর মুছে যায়
- URL এ কিছু দেখায় না

---

### ৪. upload.html (Thymeleaf Template)

#### ফর্ম

```html
<form th:action="@{/upload}" method="post" enctype="multipart/form-data">
    <input type="file" name="files" multiple
           accept=".jpg,.jpeg,.png,.gif,.css,.html,.htm">
    <button type="submit">Upload Files</button>
</form>
```

| অ্যাট্রিবিউট | ব্যাখ্যা |
|-------------|---------|
| `th:action="@{/upload}"` | Thymeleaf URL এক্সপ্রেশন - context path সামলায় |
| `enctype="multipart/form-data"` | ফাইল আপলোডের জন্য আবশ্যক |
| `multiple` | একাধিক ফাইল সিলেক্ট করতে দেয় |
| `accept` | ব্রাউজারে ফাইল ফিল্টার (সার্ভারেও চেক হয়) |

#### এরর দেখানো

```html
<div th:if="${error}" class="alert alert-error" th:text="${error}"></div>
```

- `th:if="${error}"` - error থাকলেই দেখাবে
- `th:text="${error}"` - error মেসেজ দেখাবে

#### রেজাল্ট লুপ

```html
<div th:each="result : ${results}"
     th:classappend="${result.success} ? 'success' : 'failure'">

    <span th:text="${result.filename}"></span>

    <span th:if="${result.success}" th:text="${result.savedPath}"></span>
    <span th:unless="${result.success}" th:text="${result.message}"></span>
</div>
```

| Thymeleaf | ব্যাখ্যা |
|-----------|---------|
| `th:each` | লুপ - প্রতিটি result এর জন্য |
| `th:classappend` | শর্ত অনুযায়ী CSS ক্লাস যোগ |
| `th:if` | শর্ত সত্য হলে দেখায় |
| `th:unless` | শর্ত মিথ্যা হলে দেখায় |

---

### ৫. application.properties (কনফিগারেশন)

```properties
# মাল্টিপার্ট ফাইল আপলোড সেটিংস
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=5MB      # প্রতি ফাইল সর্বোচ্চ 5MB
spring.servlet.multipart.max-request-size=25MB  # মোট রিকোয়েস্ট সর্বোচ্চ 25MB

# আপলোড পাথ
app.upload.base-path=src/main/resources/static
```

---

## ডেটা ফ্লো

```
১. ব্যবহারকারী ফাইল সিলেক্ট করে
          ↓
২. "Upload" বাটনে ক্লিক
          ↓
³. ব্রাউজার POST /upload পাঠায় (multipart/form-data)
          ↓
⁴. Controller রিকোয়েস্ট গ্রহণ করে
          ↓
⁵. Service প্রতিটি ফাইল প্রসেস করে:
   • ফাইলনাম পরিষ্কার করে
   • এক্সটেনশন চেক করে
   • সঠিক ফোল্ডারে সেভ করে
          ↓
⁶. Controller রেজাল্ট Flash Attribute এ রাখে
          ↓
⁷. redirect:/ পাঠায়
          ↓
⁸. ব্রাউজার GET / রিকোয়েস্ট করে
          ↓
⁹. Controller upload.html রেন্ডার করে (রেজাল্টসহ)
          ↓
¹⁰. ব্যবহারকারী আপলোডের ফলাফল দেখে
```

---

## নিরাপত্তা বৈশিষ্ট্য

| বৈশিষ্ট্য | বাস্তবায়ন |
|----------|-----------|
| ফাইল টাইপ ভ্যালিডেশন | Whitelist approach - শুধু নির্দিষ্ট এক্সটেনশন অনুমোদিত |
| ফাইলনাম স্যানিটাইজেশন | স্পেশাল ক্যারেক্টার রিমুভ - path traversal প্রতিরোধ |
| সাইজ লিমিট | প্রতি ফাইল 5MB, মোট 25MB |
| PRG প্যাটার্ন | ডুপ্লিকেট সাবমিশন প্রতিরোধ |

---

## চালানোর নির্দেশনা

```bash
# প্রজেক্ট ডিরেক্টরিতে যান
cd template-upload

# অ্যাপ্লিকেশন চালু করুন
./mvnw spring-boot:run

# ব্রাউজারে খুলুন
# http://localhost:8080
```

---

## প্রযুক্তি স্ট্যাক

- **Java 21** - প্রোগ্রামিং ল্যাঙ্গুয়েজ
- **Spring Boot 4.0.0** - ফ্রেমওয়ার্ক
- **Spring MVC** - ওয়েব লেয়ার
- **Thymeleaf** - টেমপ্লেট ইঞ্জিন
- **Maven** - বিল্ড টুল
