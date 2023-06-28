package com.mypill.domain.comment.service;

import com.mypill.domain.comment.dto.CommentRequest;
import com.mypill.domain.comment.dto.CommentResponse;
import com.mypill.domain.comment.entity.Comment;
import com.mypill.domain.comment.repository.CommentRepository;
import com.mypill.domain.member.entity.Member;
import com.mypill.domain.post.entity.Post;
import com.mypill.domain.post.repository.PostRepository;
import com.mypill.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public RsData<Comment> create(CommentRequest commentRequest, Member member, String postIdStr) {
        Long postId = Long.parseLong(postIdStr);
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return RsData.of("F-1", "존재하지 않는 게시물입니다.");
        }
        Comment comment = Comment.builder()
                .post(post)
                .writer(member)
                .content(commentRequest.getNewContent())
                .build();
        commentRepository.save(comment);
        return RsData.of("S-1", "댓글 등록이 완료되었습니다.", comment);
    }

    public CommentResponse update(CommentRequest commentRequest, Member member, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        String newComment = commentRequest.getNewContent();
        if (comment == null) {
            new CommentResponse("1", "존재하지 않는 댓글입니다.", newComment);
        }
        if (!Objects.equals(comment.getWriter().getId(), member.getId())) {
            return new CommentResponse("2", "본인 댓글만 수정할 수 있습니다.", newComment);
        }
        comment.update(newComment);
        commentRepository.save(comment);
        return new CommentResponse("0", "성공", newComment);
    }

    public RsData<Comment> delete(Member member, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            return RsData.of("F-1", "존재하지 않는 댓글입니다.");
        }
        if (!Objects.equals(comment.getWriter().getId(), member.getId())) {
            return RsData.of("F-2", "본인 댓글만 삭제할 수 있습니다.");
        }
        comment = comment.toBuilder()
                .deleteDate(LocalDateTime.now())
                .build();
        commentRepository.save(comment);
        return RsData.of("S-1", "댓글 삭제가 완료되었습니다.", comment);
    }
}