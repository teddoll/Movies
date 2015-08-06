Movies
=======

A simple application backed by the themoviedb.org API providing the most popular movies playing.


themoviedb.org API key
--------

To compile and run this app you will need to [create an account][1] with
themoviedb.org and request an API Key.

Once you have an API key insert into the app build.gradle (app/build.gradle) in both the release
and debug build types.
e.g. buildConfigField "string", "API_KEY", "<Your API Key Here>"


License
--------

    Copyright 2015 Theodore Doll

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


 [1]: https://www.themoviedb.org/account/signup