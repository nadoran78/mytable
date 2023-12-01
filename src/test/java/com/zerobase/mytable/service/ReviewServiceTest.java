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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private StoreRepository storeRepository;
    @InjectMocks
    private ReviewService reviewService;

    // 리뷰 작성 성공 테스트
    @Test
    void successCustomerCreateReview() {
        //given
        Customer customer = mock(Customer.class);
        Store store = Store.builder()
                .storename("포차")
                .build();
        Reservation reservation = Reservation.builder().store(store).build();

        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        given(customerRepository.getByUid(anyString()))
                .willReturn(customer);
        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.of(store));
        given(customer.getReservations())
                .willReturn(List.of(reservation));
        given(customer.getName())
                .willReturn("원빈");
        given(reviewRepository.save(any(Review.class)))
                .will(returnsFirstArg());
        //when
        ReviewDto request = ReviewDto.builder()
                .storename("포차")
                .title("맛집이에요")
                .text("번창하세요")
                .build();
        ReviewDto reviewDto = reviewService.customerCreateReview(
                "123", request);
        //then
        assertEquals(reviewDto.getStorename(), "포차");
        assertEquals(reviewDto.getTitle(), "맛집이에요");
        assertEquals(reviewDto.getText(), "번창하세요");
        assertEquals(reviewDto.getName(), "원*");
    }

    // 리뷰 작성할 경우 점포 조회 시 저장된 점포가 아니면 예외 처리
    @Test
    void customerCreateReview_NotFoundStore() {
        //given
        Customer customer = mock(Customer.class);

        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        given(customerRepository.getByUid(anyString()))
                .willReturn(customer);
        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.empty());
        //when
        ReviewDto request = ReviewDto.builder()
                .storename("포차")
                .title("맛집이에요")
                .text("번창하세요")
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> reviewService.customerCreateReview("123", request));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.NOT_FOUND_STORE);
    }

    // 리뷰 작성할 경우 고객이 해당 점포에 예약 한 경우가 없을 경우 예외 처리
    @Test
    void customerCreateReview_DidNotUseThisStore() {
        //given
        Customer customer = mock(Customer.class);
        Store reservationStore = Store.builder()
                .storename("포차")
                .build();
        Store requestStore = Store.builder()
                .storename("포장마차")
                .build();
        Reservation reservation = Reservation.builder().store(reservationStore).build();

        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        given(customerRepository.getByUid(anyString()))
                .willReturn(customer);
        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.of(requestStore));
        given(customer.getReservations())
                .willReturn(List.of(reservation));
        //when
        ReviewDto request = ReviewDto.builder()
                .storename("포장마차")
                .title("맛집이에요")
                .text("번창하세요")
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> reviewService.customerCreateReview("123", request));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.DID_NOT_USE_THIS_STORE);
    }

    // 점포 별 리뷰 리스트 조회 성공 테스트
    @Test
    void successGetReviewsByStore() {
        //given
        Store store = mock(Store.class);
        Customer customer = Customer.builder().name("류승룡").build();
        List<Review> reviews = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Review review = Review.builder()
                    .id((long) i)
                    .customer(customer)
                    .store(store)
                    .title("맛있다")
                    .text("정말로 맛있다.")
                    .build();
            review.setCreatedAt(LocalDateTime.now().minusMonths(1).plusDays(i));
            reviews.add(review);
        }

        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.of(store));
        given(store.getReviews()).willReturn(reviews);
        given(store.getStorename()).willReturn("포장마차");

        //when
        Page<ReviewDto> page = reviewService.getReviewsByStore(
                "포장마차", PageRequest.of(0, 5));
        //then
        assertEquals(page.getContent().get(0).getId(), 3);
        assertEquals(page.getContent().get(1).getId(), 2);
        assertEquals(page.getContent().get(2).getId(), 1);
    }

    // 점포 별 리뷰 리스트 조회 시 찾는 점포 없을 경우 예외처리
    @Test
    void getReviewsByStore_NotFoundStore() {
        //given
        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.empty());
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> reviewService.getReviewsByStore(
                        "abc", PageRequest.of(0, 5)));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.NOT_FOUND_STORE);
    }

    // 리뷰 상세 조회 성공 테스트
    @Test
    void successGetReviewDetail() {
        //given
        Customer customer = Customer.builder().name("류승룡").build();
        Store store = Store.builder().storename("포장마차").build();
        Review review = Review.builder()
                .id((long) 1)
                .customer(customer)
                .store(store)
                .title("맛있다")
                .text("정말로 맛있다.")
                .build();
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(review));
        //when
        ReviewDto reviewDto = reviewService.getReviewDetail(1L);
        //then
        assertEquals(reviewDto.getId(), 1L);
        assertEquals(reviewDto.getName(), "류*룡");
        assertEquals(reviewDto.getStorename(), "포장마차");
        assertEquals(reviewDto.getTitle(), "맛있다");
        assertEquals(reviewDto.getText(), "정말로 맛있다.");
    }

    // 리뷰 상세 조회 시 저장된 리뷰 없을 경우 예외 처리
    @Test
    void getReviewDetail_NotFoundReview() {
        //given
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> reviewService.getReviewDetail(1L));

        //then
        assertEquals(customException.getErrorCode(), ErrorCode.NOT_FOUND_REVIEW);
    }

    // 리뷰 수정 성공 테스트
    @Test
    void successUpdateReview() {
        //given
        Customer customer = Customer.builder().uid("12").name("류승룡").build();
        Store store = Store.builder().storename("포장마차").build();
        Review review = Review.builder()
                .id(1L)
                .customer(customer)
                .store(store)
                .title("맛있다")
                .text("정말로 맛있다.")
                .build();
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(review));
        given(tokenProvider.getUid(anyString()))
                .willReturn("12");
        given(reviewRepository.save(any(Review.class)))
                .will(returnsFirstArg());
        //when
        ReviewDto request = ReviewDto.builder()
                .storename("포장마차")
                .title("맛집이에요")
                .text("번창하세요")
                .build();
        ReviewDto reviewDto = reviewService.updateReview(
                "123", 1L, request);
        //then
        assertEquals(reviewDto.getName(), "류*룡");
        assertEquals(reviewDto.getStorename(), "포장마차");
        assertEquals(reviewDto.getTitle(), "맛집이에요");
        assertEquals(reviewDto.getText(), "번창하세요");
    }

    // 리뷰 수정 시 수정하려는 리뷰가 등록되어 있지 않을 경우 예외 처리
    @Test
    void updateReview_NotFoundReview() {
        //given
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        ReviewDto request = ReviewDto.builder()
                .storename("포장마차")
                .title("맛집이에요")
                .text("번창하세요")
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> reviewService.updateReview("12" , 1L, request));

        //then
        assertEquals(customException.getErrorCode(), ErrorCode.NOT_FOUND_REVIEW);
    }

    // 리뷰 수정 시 작성자가 아닌 다른 사람이 수정하려는 경우 예외 처리
    @Test
    void updateReview_OnlyWorksWithWriter() {
        //given
        Customer customer = Customer.builder().uid("123").name("류승룡").build();
        Store store = Store.builder().storename("포장마차").build();
        Review review = Review.builder()
                .id(1L)
                .customer(customer)
                .store(store)
                .title("맛있다")
                .text("정말로 맛있다.")
                .build();
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(review));
        given(tokenProvider.getUid(anyString()))
                .willReturn("12");

        //when
        ReviewDto request = ReviewDto.builder()
                .storename("포장마차")
                .title("맛집이에요")
                .text("번창하세요")
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> reviewService.updateReview("12" , 1L, request));

        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ONLY_WORKS_WITH_WRITER);
    }

    // 고객이 리뷰 수정 시 점포명을 수정한 경우 예외 처리
    @Test
    void updateReview_CannotUpdateStorename() {
        //given
        Customer customer = Customer.builder().uid("12").build();

        Store store = Store.builder().storename("포장마차").build();
        Review review = Review.builder()
                .id(1L)
                .customer(customer)
                .store(store)
                .title("맛있다")
                .text("정말로 맛있다.")
                .build();
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(review));
        given(tokenProvider.getUid(anyString()))
                .willReturn("12");

        //when
        ReviewDto request = ReviewDto.builder()
                .storename("포차")
                .title("맛집이에요")
                .text("번창하세요")
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> reviewService.updateReview("12" , 1L, request));

        //then
        assertEquals(customException.getErrorCode(), ErrorCode.CANNOT_UPDATE_STORENAME);
    }

    // 고객이 리뷰 삭제 성공 테스트
    @Test
    void successDeleteReview_Customer() {
        //given
        Customer customer = Customer.builder().uid("12").name("류승룡").build();
        Partner partner = Partner.builder().uid("34").build();
        Store store = Store.builder().storename("포장마차").partner(partner).build();
        Review review = Review.builder()
                .id(1L)
                .customer(customer)
                .store(store)
                .title("맛있다")
                .text("정말로 맛있다.")
                .build();
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(review));
        given(tokenProvider.getRole(anyString()))
                .willReturn(List.of("ROLE_CUSTOMER"));
        given(tokenProvider.getUid(anyString()))
                .willReturn("12");

        //when
        CommonResponse commonResponse =
                reviewService.deleteReview("123", 1L);
        //then
        assertEquals(commonResponse, CommonResponse.SUCCESS);
    }

    // 파트너 리뷰 삭제 성공 테스트
    @Test
    void successDeleteReview_Partner() {
        //given
        Customer customer = Customer.builder().uid("12").name("류승룡").build();
        Partner partner = Partner.builder().uid("34").build();
        Store store = Store.builder().storename("포장마차").partner(partner).build();
        Review review = Review.builder()
                .id(1L)
                .customer(customer)
                .store(store)
                .title("맛있다")
                .text("정말로 맛있다.")
                .build();
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(review));
        given(tokenProvider.getRole(anyString()))
                .willReturn(List.of("ROLE_PARTNER"));
        given(tokenProvider.getUid(anyString()))
                .willReturn("34");

        //when
        CommonResponse commonResponse =
                reviewService.deleteReview("123", 1L);
        //then
        assertEquals(commonResponse, CommonResponse.SUCCESS);
    }

    // 리뷰 삭제 시 삭제하려는 리뷰가 등록되어 있지 않을 경우 예외 처리
    @Test
    void deleteReview_NotFoundReview() {
        //given
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> reviewService.deleteReview("12" , 1L));

        //then
        assertEquals(customException.getErrorCode(), ErrorCode.NOT_FOUND_REVIEW);
    }

    // 리뷰 작성자가 아닌 다른 고객이 리뷰 삭제하려는 경우 예외 처리
    @Test
    void deleteReview_OnlyWorksWithWriter() {
        //given
        Customer customer = Customer.builder().uid("12").name("류승룡").build();
        Partner partner = Partner.builder().uid("34").build();
        Store store = Store.builder().storename("포장마차").partner(partner).build();
        Review review = Review.builder()
                .id(1L)
                .customer(customer)
                .store(store)
                .title("맛있다")
                .text("정말로 맛있다.")
                .build();
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(review));
        given(tokenProvider.getRole(anyString()))
                .willReturn(List.of("ROLE_CUSTOMER"));
        given(tokenProvider.getUid(anyString()))
                .willReturn("34");
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> reviewService.deleteReview("12" , 1L));

        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ONLY_WORKS_WITH_WRITER);
    }

    // 리뷰 작성된 점포의 점주가 아닌 다른 파트너가 리뷰 삭제하려는 경우 예외 처리
    @Test
    void deleteReview_AccessOnlyStoreOwner() {
        //given
        Customer customer = Customer.builder().uid("12").name("류승룡").build();
        Partner partner = Partner.builder().uid("34").build();
        Store store = Store.builder().storename("포장마차").partner(partner).build();
        Review review = Review.builder()
                .id(1L)
                .customer(customer)
                .store(store)
                .title("맛있다")
                .text("정말로 맛있다.")
                .build();
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(review));
        given(tokenProvider.getRole(anyString()))
                .willReturn(List.of("ROLE_PARTNER"));
        given(tokenProvider.getUid(anyString()))
                .willReturn("12");
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> reviewService.deleteReview("12" , 1L));

        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ACCESS_ONLY_STORE_OWNER);
    }

}