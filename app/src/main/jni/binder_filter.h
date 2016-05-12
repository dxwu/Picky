//
// Created by David Wu on 5/8/16.
//

#ifndef PICKY_BINDER_FILTER_H
#define PICKY_BINDER_FILTER_H

struct bf_user_filter {
    int level_value_no_BT;
    int level_value_with_BT;

    char** intents;
    int intents_len;
};

#endif //PICKY_BINDER_FILTER_H
