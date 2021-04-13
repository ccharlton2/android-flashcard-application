# FlashcardApplication

 - [FlashcardApplication](#flashcardapplication)

   - [What is this?](#what-is-this)

   - [How is it used?](#how-is-it-used)

   - [Cool Features](#cool-features)

   - [Other Features](#other-features)

   - [Where the interesting code is](#where-the-interesting-code-is)

       - [flashcard_flashcard_flip_view.xml](#flashcardflashcardflipviewxml)

       - [CustomFlashcardGroupRecyclerViewAdapter](#customflashcardgrouprecyclerviewadapter)

       - [FlashcardActivity](#flashcardactivity)

   - [3rd Party Libraries](#3rd-party-libraries)

     - [Useful Links](#useful-links)

## What is this?

This application is intended to be used as a study aid. Users can create flashcards for various subjects/categories.

## How is it used?

1. Add a category
2. Add a group
3. Click on a group
4. Add flashcards

## Cool Features

- Flashcards can be shuffled
- Mass delete groups by long pressing to select the groups you want to delete
- Groups can be filtered by category
- Application styling using [Material Design Components](https://material.io/components?platform=android)

## Other Features

- Flashcard flipping behavior can be modified from the preferences screen
- Groups can be updated by long pressing a group and tapping the "edit" icon
- Flashcards can be given hints that you can see by tapping the "question mark" icon
- You can navigate to the start of a flashcard set by tapping the "reverse spinning arrow"

## Where the interesting code is

#### flashcard_flashcard_flip_view.xml

The following tags are required in order to use the [EasyFlipView](https://github.com/wajahatkarim3/EasyFlipView).
```
<com.wajahatkarim3.easyflipview.EasyFlipView></com.wajahatkarim3.easyflipview.EasyFlipView>
```
You can define which direction the view flips
`app:flipType="vertical" || app:flipType="horizontal"`

The flip animation duration, as well as auto-flip-back behavior can be modified as well:
```
app:autoFlipBack="true"
app:flipDuration="600"
app:autoFlipBackTime="3000"
```

For more info please refer to the documentation [here](https://github.com/wajahatkarim3/EasyFlipView).

#### CustomFlashcardGroupRecyclerViewAdapter

There are three classes required to make this work:
1. FlashcardGroup
    - The model
2. FlashcardGroupViewHolder
    - An intermediary class that models the FlashcardGroup view layout
3. CustomFlashcardGroupRecyclerViewAdapter
    - This class extends the default RecyclerViewAdapter class
    - `onBindViewHolder()` is responsible for setting the listeners on each view in the RecyclerView
    - `getFilter()` is a built in method implemented by the [Filterable](https://developer.android.com/reference/android/widget/Filterable) Android class.

    The following line of code is an example of how to use `getFilter()`:
    ```
    mAdapter.getFilter().filter(filter);
    ```
    `filter` is of the type `CharSequence` (basically a `String`). It's set to either a blank string or a category the user picks from the filter.

    The following code is responsible for the checking behavior observed when a user long presses a flashcard group view:
    ```
            flashcardGroupCardView.setOnLongClickListener(
                new View.OnLongClickListener()
                {
                    public boolean onLongClick(View view)
                    {
                        ...
                        // toggles the checked state
                        ((MaterialCardView) view).setChecked(!((MaterialCardView) view).isChecked());
                        ...
                    }
                }
            );
    ```

#### FlashcardActivity
- `shuffleFlashcards(String groupId)` is responsible for rebinding the RecyclerView with a shuffled ArrayList.

The following line demonstrates how the ArrayList is shuffled:

```
Collections.shuffle(allFlashcards);
```

You can find out more about `Collections.shuffle()` [here](https://www.tutorialspoint.com/java/util/collections_shuffle.htm).

## 3rd Party Libraries

- [EasyFlipView](https://github.com/wajahatkarim3/EasyFlipView)
    - This is a really easy to use and well documented library that allows you to create simple "flippable" views

### Useful Links

- [Material Design](https://material.io/)
- [EasyFlipView](https://github.com/wajahatkarim3/EasyFlipView)
- [Material Icons](https://fonts.google.com/icons)
- [Material Color Interface Tool](https://material.io/resources/color/#!/?view.left=0&view.right=0)