# atd-ass4
Twitter stream for artists.

Technical explanations further on.

# Installation
The zip file provided is after compilation of angular and typescript files so execution only requires to run the backend (Steps 1,4,5,6)
In order to compile from scratch
1. In .\src\backend run `npm update`
2. In .\src\frontend run `npm update`
3. Using angular\cli, run `ng build` in .\src\backend
4. Compile backend typescript ('tsc -w' on root folder)
5. Run src\backend\index.js using node ('npm start')
6. Run mongodb server

# Main features

## Posts are
    
* Author/Co Authors
* Image
* Description
* hashtags/media types
* Date creation (author provided)
* Date upload

## Users can follow 
    
* Authors
* Hashtags

## Post interaction is
    
* Like
* Comment

## Searches:
* Authors
* Hashtags
    
## Main UI includes
* Most active users
* Popular hashtags

    
# Backend model
## Users have
* Display Name
* Description
* Avatar
### Authentication
  * Local credentials (user/password hashed)
* Email
* Avatar   
* Description 
* User Follow list
* Hashtag follow list


## Posts have
Posts are composed of a post overview showing the final piece of art and sub posts explaining the steps used to reach the final result.
* Description
* List of authors of the piece
* Hashtags
* List of users who have liked the post
* Comments
* A list of sub posts, each containing
** Title
** Description
** Image
** List of materials used in this post

## Hashtags
* Name
* Counter of usage


## Analytics
* Per week
  * Active hashtags
  * Active users

# Code referenced from
## Style
* General theme
  * https://www.bootstrapzero.com/bootstrap-template/facebook
* Login modal
  * https://bootsnipp.com/snippets/featured/clean-modal-login-form
* registration modal
  * https://bootsnipp.com/snippets/DVXQa
* profile cards
  * https://bootsnipp.com/snippets/M23j
* edit profile
  * https://bootsnipp.com/snippets/86dXW
* profile page
  * https://bootsnipp.com/snippets/featured/people-card-with-tabs
* carousel modal
  * https://bootsnipp.com/snippets/featured/carousel-inside-modal
  
## Directives
* Bindable datetime directive
  * http://plnkr.co/edit/9nMxggSXMW0pK30XK24i
  
# DB Schema
The DB is based on Mongoose, a node ORM library. There are 3 main schemas.

## Hashtags
* Name of hashtag
* Number of mentions of this hashtag

## Users
* Display name
* Email
* Password hash (salt+hash using bcrypt)
* List of following user IDs
* List of following hash IDs
* Avatar/Profile picture
* Description

## Posts
* Post title
* Post description
* Creation date
* Hashtags
* Authors of the post
* List of voters

Further, each post has a list of "steps", used to create the final product. Each step contains
* Title
* Description
* Image 
* List of materials used for this step

# List of dependencies
The entire project is written using TypeScript and Angular 4 for the frontend and express-js/mongoose for the backend.
## Backend
* Refer to package.json
## Front end
* Refer to src/frontend/package.json