//
// Created by David Wu on 5/8/16.
//

#ifndef PICKY_BINDER_FILTER_H
#define PICKY_BINDER_FILTER_H

struct bf_user_filter {
    int action;
    int uid;
    char* message;
    char* data;
};

#endif //PICKY_BINDER_FILTER_H
