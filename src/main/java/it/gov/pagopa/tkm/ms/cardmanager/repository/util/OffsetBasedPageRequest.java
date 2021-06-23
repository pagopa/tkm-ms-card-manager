package it.gov.pagopa.tkm.ms.cardmanager.repository.util;

import org.jetbrains.annotations.*;
import org.springframework.data.domain.*;

public class OffsetBasedPageRequest implements Pageable {

    private final int limit;
    private final int offset;

    private final Sort sort = Sort.by("id");

    public OffsetBasedPageRequest(int limit, int offset) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must not be less than one");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset index must not be less than zero");
        }
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @NotNull
    @Override
    public Sort getSort() {
        return sort;
    }

    @NotNull
    @Override
    public Pageable next() {
        return new OffsetBasedPageRequest(getPageSize(), (int)(getOffset() + getPageSize()));
    }

    public Pageable previous() {
        return hasPrevious() ? new OffsetBasedPageRequest(getPageSize(), (int) (getOffset() - getPageSize())) : this;
    }

    @NotNull
    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @NotNull
    @Override
    public Pageable first() {
        return new OffsetBasedPageRequest(getPageSize(), 0);
    }

    @Override
    public boolean hasPrevious() {
        return offset > limit;
    }

}
