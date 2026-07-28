# Story 7.1: Image Upload Path Resolution & PDF Header Fixes

Status: done

## Story

As an Event Coordinator,
I want the header image uploads to work robustly during local testing and render correctly inside the generated PDF ticket batches,
so that I can customize the visual branding of physical ticket prints without encountering server errors or missing images.

## Acceptance Criteria

1. **Given** a multipart image file upload request, **When** the admin submits a custom header image, **Then** the backend must save it using standard file operations (`Files.copy`) instead of servlet-container specific transfer methods that fail on relative paths.
2. **Given** an uploaded header image, **When** retrieving it via API or generating the ticket PDF batch, **Then** the path must be dynamically resolved relative to the workspace, ensuring the resource loads correctly across different local execution directories.

## Tasks / Subtasks

- [x] **Task 1: Backend (Services)**
  - [x] Refactor path resolution in `EventService.java` to dynamically locate the uploads folder relative to the user's workspace directory.
  - [x] Replace `MultipartFile.transferTo` with safe, absolute-path resolved `Files.copy` to avoid internal Tomcat temporary directory errors.
- [x] **Task 2: Backend (PDF Generator)**
  - [x] Ensure that the `PdfGenerator` references the correctly resolved local path of the header image when constructing the PDF document template.

## Dev Notes

- Replaced servlet-native path handling with absolute directory resolution.
- Resolves the issue where header images failed to display in PDFs or returned 500 error codes on local machines depending on where the backend process was launched.
