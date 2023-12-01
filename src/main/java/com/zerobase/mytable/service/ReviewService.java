package com.zerobase.mytable.service;

import com.zerobase.mytable.domain.*;
import com.zerobase.mytable.dto.ReviewDto;
import com.zerobase.mytable.exception.CustomException;
import com.zerobase.mytable.repository.CustomerRepository;
import com.zerobase.mytable.repository.ReviewRepository;
import com.zerobase.mytable.repository.StoreRepository;
import com.zerobase.mytable.security.TokenProvider;
import com.zerobase.mytable.type.CommonResponse;
import com.zerobase.mytable.type.ErrorCode;
import com.zerobase.mytable.type.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TokenProvider tokenProvider;
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;

    // 고객 리뷰 작성
    @Transactional
    public ReviewDto customerCreateReview(String token, ReviewDto request) {
        Customer customer = customerRepository.getByUid(
                tokenProvider.getUid(token));

        Store store = storeRepository.findByStorename(request.getStorename())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STORE));

        // 해당 점포에 예약한 이력 있는지 조회
        List<Reservation> reservations = customer.getReservations().stream()
                .filter(reservation -> reservation.getStore().equals(store))
                .collect(Collectors.toList());
        if (reservations.isEmpty()) {
            throw new CustomException(ErrorCode.DID_NOT_USE_THIS_STORE);
        }

        Review savedReview = reviewRepository.save(
                Review.from(request, customer, store));

        return ReviewDto.from(savedReview);
    }

    // 점포 별 리뷰 리스트 조회
    public Page<ReviewDto> getReviewsByStore(String storename, PageRequest pageRequest) {
        Store store = storeRepository.findByStorename(storename)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STORE));

        List<ReviewDto> reviewDtos = store.getReviews().stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedAt).reversed())
                .map(ReviewDto::from)
                .collect(Collectors.toList());

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), reviewDtos.size());

        return new PageImpl<>(
                reviewDtos.subList(start, end),
                pageRequest,
                reviewDtos.size());
    }

    // 리뷰 내용 상세조회
    public ReviewDto getReviewDetail(Long reviewId) {
        Review review = getReview(reviewId);
        return ReviewDto.from(review);
    }

    // 리뷰 수정
    @Transactional
    public ReviewDto updateReview(String token, Long reviewId, ReviewDto request) {
        Review review = getReview(reviewId);

        if (!review.getCustomer().getUid().equals(tokenProvider.getUid(token))) {
            throw new CustomException(ErrorCode.ONLY_WORKS_WITH_WRITER);
        }

        if (!review.getStore().getStorename().equals(request.getStorename())) {
            throw new CustomException(ErrorCode.CANNOT_UPDATE_STORENAME);
        }

        review.setTitle(request.getTitle());
        review.setText(request.getText());

        return ReviewDto.from(reviewRepository.save(review));
    }

    // 고객 리뷰 삭제
    public CommonResponse deleteReview(String token, Long reviewId) {
        Review review = getReview(reviewId);

        if (tokenProvider.getRole(token).equals(
                List.of(UserType.ROLE_CUSTOMER.toString()))) {
            if (!review.getCustomer().getUid().equals(tokenProvider.getUid(token))) {
                throw new CustomException(ErrorCode.ONLY_WORKS_WITH_WRITER);
            }
        } else if(tokenProvider.getRole(token).equals(
                List.of(UserType.ROLE_PARTNER.toString()))) {
            if (!review.getStore().getPartner().getUid().equals(
                    tokenProvider.getUid(token))) {
                throw new CustomException(ErrorCode.ACCESS_ONLY_STORE_OWNER);
            }
        }

        reviewRepository.delete(review);

        return CommonResponse.SUCCESS;
    }

    private Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_REVIEW));
    }


}
