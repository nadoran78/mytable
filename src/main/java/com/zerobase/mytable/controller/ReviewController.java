package com.zerobase.mytable.controller;

import com.zerobase.mytable.dto.ReviewDto;
import com.zerobase.mytable.service.ReviewService;
import com.zerobase.mytable.type.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/store/review")
public class ReviewController {

    private final ReviewService reviewService;

    // 고객 리뷰 작성
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ReviewDto customerCreateReview(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @Valid @RequestBody ReviewDto request) {
        return reviewService.customerCreateReview(token, request);
    }

    // 점포 별 리뷰 리스트 조회
    @GetMapping("/list")
    public Page<ReviewDto> getReviewsByStore(@RequestParam String storename,
                                             @RequestParam Integer page,
                                             @RequestParam Integer size) {
        return reviewService.getReviewsByStore(storename, PageRequest.of(page, size));
    }

    // 리뷰 상세 조회
    @GetMapping
    public ReviewDto getReviewDetail(@RequestParam Long reviewId) {
        return reviewService.getReviewDetail(reviewId);
    }

    // 리뷰 수정
    @PutMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ReviewDto updateReview(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam Long reviewId,
            @Valid @RequestBody ReviewDto request) {
        return reviewService.updateReview(token, reviewId, request);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PARTNER')")
    public CommonResponse deleteReview(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam Long reviewId) {
        return reviewService.deleteReview(token, reviewId);
    }
}
