package com.example.yaho.service.DiaryService;

import com.example.yaho.domain.Member;
import com.example.yaho.repository.MemberRepository;
import com.example.yaho.apiPayload.code.status.ErrorStatus;
import com.example.yaho.apiPayload.exception.handler.GameIdHandler;
import com.example.yaho.aws.s3.AmazonS3Manager;
import com.example.yaho.converter.DiaryConverter;
import com.example.yaho.domain.Diary;
import com.example.yaho.domain.Game;
import com.example.yaho.domain.Member;
import com.example.yaho.domain.Uuid;
import com.example.yaho.domain.enums.Location;
import com.example.yaho.repository.DiaryRepository;
import com.example.yaho.repository.GameRepository;
import com.example.yaho.repository.MemberRepository;
import com.example.yaho.repository.UuidRepository;
import com.example.yaho.web.dto.DiaryRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.yaho.domain.Diary;
import com.example.yaho.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryQueryServiceImpl implements DiaryQueryService{

    private final DiaryRepository diaryRepository;
    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;
    private final AmazonS3Manager s3Manager;
    private final UuidRepository uuidRepository;


    @Override
    public String getEmotionImageUrl(DiaryRequestDTO.EmotionDto request) {

        String emotionImageUrl = "";

        switch (request.getEmotionImage().intValue()) {
            case 1:
                emotionImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/emotions/1.svg";
                break;
            case 2:
                emotionImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/emotions/2.svg";
                break;
            case 3:
                emotionImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/emotions/3.svg";
                break;
            case 4:
                emotionImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/emotions/4.svg";
                break;
            case 5:
                emotionImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/emotions/5.svg";
                break;
            case 6:
                emotionImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/emotions/6.svg";
                break;
            case 7:
                emotionImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/emotions/7.svg";
                break;
            case 8:
                emotionImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/emotions/8.svg";
                break;
            case 9:
                emotionImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/emotions/9.svg";
                break;
        }
        return emotionImageUrl;
    }

    @Override
    public String getFavoriteClubImageUrl(Long memberId) {

        Member member = memberRepository.findById(memberId).get();

        String favoriteClubImageUrl = "";

        switch (member.getFavoriteClub()) {
            case SSG_LANDERS:
                favoriteClubImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/clubs/SSG_LANDERS.svg";
                break;
            case LG_TWINS:
                favoriteClubImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/clubs/LG_TWINS.svg";
                break;
            case KT_WIZ:
                favoriteClubImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/clubs/KT_WIZ.svg";
                break;
            case NC_DINOS:
                favoriteClubImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/clubs/NC_DINOS.svg";
                break;
            case DOOSAN_BEARS:
                favoriteClubImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/clubs/DOOSAN_BEARS.svg";
                break;
            case KIA_TIGERS:
                favoriteClubImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/clubs/KIA_TIGERS.svg";
                break;
            case LOTTE_GIANTS:
                favoriteClubImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/clubs/LOTTE_GIANTS.svg";
                break;
            case SAMSUNG_LIONS:
                favoriteClubImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/clubs/SAMSUNG_LIONS.svg";
                break;
            case HANWHA_EAGLES:
                favoriteClubImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/clubs/HANWHA_EAGLES.svg";
                break;
            case KIWOOM_HEROES:
                favoriteClubImageUrl = "https://yahobucket.s3.ap-northeast-2.amazonaws.com/clubs/KIWOOM_HEROES.svg";
                break;
        }
        return favoriteClubImageUrl;

    }
  
    @Override
    @Transactional
    public Diary getDiary(Long memberId, LocalDate date) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));


        Diary diary = diaryRepository.findByDateAndMember(date, member)
                .orElseThrow(() -> new RuntimeException("Diary not found"));

        return diary;
    }


    @Override
    @Transactional
    public Diary modifyDiary(Long memberId, DiaryRequestDTO.ModifyDto request) {
        // Retrieve the diary based on the date and location provided in the request
        Game game = gameRepository.findByDateAndLocation(request.getDate(), getLocationFromRequest(request.getLocation()))
                .orElseThrow(() -> new RuntimeException("Game not found"));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));


        Diary diary = diaryRepository.findByGameAndMember(game, member).orElseThrow(() -> new GameIdHandler(ErrorStatus.GAME_ID_NOT_FOUND));


        // Modify the diary using the converter
        diary = DiaryConverter.modifyDiary(request, diary);
        diary.setGame(game);


        // Save and return the updated diary
        return diaryRepository.save(diary);
    }

    @Transactional
    @Override
    public Diary updateMvpImg(DiaryRequestDTO.MvpImageDto image, Long memberId, LocalDate date, Integer location) {
        // Retrieve the diary based on the date and location provided in the request
        Game game = gameRepository.findByDateAndLocation(date, getLocationFromRequest(location))
                .orElseThrow(() -> new RuntimeException("Game not found"));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));


        Diary diary = diaryRepository.findByGameAndMember(game, member).orElseThrow(() -> new GameIdHandler(ErrorStatus.GAME_ID_NOT_FOUND));


        // If there's an MVP picture, upload it to S3 and update the diary
        if (image.getMvpPicture() != null) {
            String uuid = UUID.randomUUID().toString();
            Uuid savedUuid = uuidRepository.save(Uuid.builder()
                    .uuid(uuid).build());

            // Generate the S3 key and upload the file
            String pictureUrl = s3Manager.uploadFile(s3Manager.generateMvpKeyName(savedUuid), image.getMvpPicture());
            System.out.println("Uploaded Image URL: " + pictureUrl); // 로그로 출력

            diary = DiaryConverter.updateMvpImage(diary, pictureUrl);
            System.out.println("Diary after image update: " + diary.getMvpImageUrl()); // 로그로 출력

        }

        // Save and return the updated diary
        return diaryRepository.save(diary);
    }

    private static Location getLocationFromRequest(int locationCode) {
        switch (locationCode) {
            case 1:
                return Location.GOCHEOK_SKY_DOME;
            case 2:
                return Location.KIA_CHAMPIONS_FIELD;
            case 3:
                return Location.DAEGU_SAMSUNG_LIONS_PARK;
            case 4:
                return Location.SAJIK_BASEBALL_STADIUM;
            case 5:
                return Location.SUWON_KT_WIZ_PARK;
            case 6:
                return Location.INCHEON_SSG_LANDERS_FIELD;
            case 7:
                return Location.JAMSIL_BASEBALL_STADIUM;
            case 8:
                return Location.CHANGWON_NC_PARK;
            default:
                throw new IllegalArgumentException("Invalid location code: " + locationCode);
        }
    }


}
