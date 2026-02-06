package com.example.todo.service;

import com.example.todo.entity.TodoAttachment;
import com.example.todo.exception.AttachmentNotFoundException;
import com.example.todo.repository.TodoAttachmentMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TodoAttachmentService {

    private final TodoAttachmentMapper attachmentMapper;
    private final FileStorageService fileStorageService;

    public TodoAttachmentService(TodoAttachmentMapper attachmentMapper, FileStorageService fileStorageService) {
        this.attachmentMapper = attachmentMapper;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public List<TodoAttachment> listByTodoId(Long todoId) {
        return attachmentMapper.findByTodoId(todoId);
    }

    @Transactional(readOnly = true)
    public TodoAttachment get(Long id) {
        TodoAttachment attachment = attachmentMapper.findById(id);
        if (attachment == null) {
            throw new AttachmentNotFoundException("Attachment not found: " + id);
        }
        return attachment;
    }

    @Transactional(rollbackFor = Exception.class)
    public TodoAttachment add(Long todoId, MultipartFile file) throws IOException {
        FileStorageService.StoredFile stored = fileStorageService.store(file);
        TodoAttachment attachment = new TodoAttachment();
        attachment.setTodoId(todoId);
        attachment.setOriginalName(stored.getOriginalName());
        attachment.setStoredName(stored.getStoredName());
        attachment.setContentType(stored.getContentType());
        attachment.setSize(stored.getSize());
        attachment.setCreatedAt(LocalDateTime.now());
        attachmentMapper.insert(attachment);
        return attachment;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) throws IOException {
        TodoAttachment attachment = get(id);
        fileStorageService.delete(attachment.getStoredName());
        attachmentMapper.delete(id);
    }
}
