package kuke.board.comment.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * '구체화된 경로(Materialized Path)' 패턴을 구현하는 임베디드 클래스입니다.
 * 댓글의 계층 구조를 하나의 문자열 경로로 저장하여, 조회 및 정렬 성능을 크게 향상시킵니다.
 *
 * 경로는 Base62로 인코딩된 5자리 청크(chunk)의 연속으로 구성됩니다.
 * - 예: 루트 댓글 "00000", 그 자식 "0000000001", 그 손자 "000000000100000"
 *
 * 이 방식을 통해 LIKE 'path%' 쿼리만으로 특정 댓글의 모든 후손을 효율적으로 찾을 수 있으며,
 * 경로 문자열의 사전적(lexicographical) 정렬 순서가 곧 댓글의 계층적 정렬 순서가 됩니다.
 */
@Getter
@ToString
@Embeddable // 이 객체가 다른 엔티티에 포함될 수 있음을 나타냅니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPath {
    private String path; // 계층 구조 경로 문자열

    // Base62 인코딩에 사용할 문자셋 (0-9, A-Z, a-z)
    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int DEPTH_CHUNK_SIZE = 5; // 각 깊이를 나타내는 청크의 길이
    private static final int MAX_DEPTH = 5; // 최대 허용 깊이

    // 가장 작은 청크와 가장 큰 청크 (형제 댓글 수의 한계를 나타냄)
    private static final String MIN_CHUNK = String.valueOf(CHARSET.charAt(0)).repeat(DEPTH_CHUNK_SIZE); // "00000"
    private static final String MAX_CHUNK = String.valueOf(CHARSET.charAt(CHARSET.length() - 1)).repeat(DEPTH_CHUNK_SIZE); // "zzzzz"

    public static CommentPath create(String path) {
        if (isDepthOverflowed(path)) {
            throw new IllegalStateException("depth overflowed");
        }
        CommentPath commentPath = new CommentPath();
        commentPath.path = path;
        return commentPath;
    }

    private static boolean isDepthOverflowed(String path) {
        return calDepth(path) > MAX_DEPTH;
    }

    private static int calDepth(String path) {
        return path.length() / DEPTH_CHUNK_SIZE;
    }

    public int getDepth() {
        return calDepth(path);
    }

    public boolean isRoot() {
        return calDepth(path) == 1;
    }

    /**
     * 현재 경로의 부모 경로를 반환합니다.
     */
    public String getParentPath() {
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE);
    }

    /**
     * 현재 댓글에 대한 새로운 자식 댓글의 경로를 생성합니다.
     * @param descendantsTopPath 현재 댓글의 후손 중 가장 정렬 순서가 높은(가장 최근의) 후손의 경로
     * @return 새로 생성된 자식 댓글의 경로
     */
    public CommentPath createChildCommentPath(String descendantsTopPath) {
        // 후손이 없으면, 첫 번째 자식 경로를 생성합니다. (예: "path" + "00000")
        if (descendantsTopPath == null) {
            return CommentPath.create(path + MIN_CHUNK);
        }
        // 후손이 있으면, 마지막 직계 자식의 경로를 찾아 1 증가시킵니다.
        String childrenTopPath = findChildrenTopPath(descendantsTopPath);
        return CommentPath.create(increase(childrenTopPath));
    }

    /**
     * 전체 후손 경로에서 직계 자식의 경로 부분만 추출합니다.
     */
    private String findChildrenTopPath(String descendantsTopPath) {
        return descendantsTopPath.substring(0, (getDepth() + 1) * DEPTH_CHUNK_SIZE);
    }

    /**
     * 경로의 마지막 청크를 Base62 기준으로 1 증가시킵니다.
     * @param path 증가시킬 경로
     * @return 1 증가된 새로운 경로
     */
    private String increase(String path) {
        String lastChunk = path.substring(path.length() - DEPTH_CHUNK_SIZE);
        if (isChunkOverflowed(lastChunk)) {
            throw new IllegalStateException("chunk overflowed");
        }

        int charsetLength = CHARSET.length();

        // Base62 문자열을 10진수 숫자로 변환
        int value = 0;
        for (char ch : lastChunk.toCharArray()) {
            value = value * charsetLength + CHARSET.indexOf(ch);
        }

        value = value + 1; // 1 증가

        // 10진수 숫자를 다시 Base62 문자열로 변환
        StringBuilder result = new StringBuilder();
        for (int i=0; i < DEPTH_CHUNK_SIZE; i++) {
            result.insert(0, CHARSET.charAt(value % charsetLength));
            value /= charsetLength;
        }

        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE) + result;
    }

    private boolean isChunkOverflowed(String lastChunk) {
        return MAX_CHUNK.equals(lastChunk);
    }

}