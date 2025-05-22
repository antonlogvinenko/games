### Features
- High priority
  - new game button
  - display the amount of cleared lines
  - display score
  - controls info
  - pause when the tab is inactive?
  - switching levels
- Lower priority
  - visual effects
  - sound effects
  - text, description
  - more work on SEO
  - more work on design
  - how to track visitors

### Bugs
- verify once more that lock js works properly
- check if in game over elements are overlapped in the very end?
- arrowdown must be handled differently - smooth descend
- pause the game when the webpage is left
- game over must be declared earlier?
- actors must return their inbox? => less code

 ### Resources
- https://domainlockjs.com
- domains
  - https://medium.com/@LovettLovett/github-pages-godaddy-f0318c2f25a
  - https://stackoverflow.com/questions/44672603/pointing-godaddy-dns-to-github-page-uses-http-over-https

### Ideas
- Copyright enforcement
 - protect from copying
   - less direct: set var, not exception
   - call from several places
   - hide reading .location .host - get property via eval?
   - send js from backend
   - send js via websockets
   - use hashing for .location .host
   - no lists of hosts
   - append/prepend random string for hashed/unhashed
   - indirect result, no direct exceptions
   - call verification from several places
   - use https://utf-8.jp/public/jjencode.html
   - use sockets https://stackoverflow.com/questions/1660060/how-to-prevent-your-javascript-code-from-being-stolen-copied-and-viewed
   - !!! You can use an ajax script injection. This is deters theft because the same domain policy which prevents XSS will make the client side script difficult to run elsewhere.
 - check https://www.goodoldtetris.com
 - icons https://icones.js.org
 - color schemes to choose
;0. if check failed
	- slow down the browser
	- show the actual website and send the user there - use their website to your ad platform
	- close the window if user disagrees

1. addition obsfuscation methods
	- compare to hash/hashes of the domain
	- js references (window / location) - are they spilled?
	- merge strings from bytes and send to js/eval to avoid detection in the output

2. check the current location with JS
	- different types of access checks in different places

3. based on user's time
	- same methods but " js works only until may 31 this year " - and somehow update on redeploy
	- web pages auto reload themselves
4. do request to backend on front (with website's url)
	- backend checks from what site the query came, and gives bad uids

 stealing precaution: hostname and verify what is visible in the obfuscated code
