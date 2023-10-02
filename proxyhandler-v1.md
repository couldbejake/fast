

We have outlined a plan for a proxy picker, to pick proxies that haven't been used.

The following restrictions are required:

- Proxies must timeout after 500 successful/failed requests in total are made.
- If a proxies invalid attempts exceeds 30 attempts within a 5 minute window, the proxy next_avaliable should be set 10 minutes in the future
- If a proxy is successful, delta should increment. If it fails it should decrement.
- If a GT (Twitter Guest Token) has been recently updated, skip the update process, as it's possible a cocurrent/paralell thread has attempted this recently.
- As threading is used, a single proxy should not be used too many times.
- Proxies should be added to a queue by a proxy gen thread that updates a FILO queue.


```
Proxy:
	usage_count -> 1 -> 500 # holds information so that one proxy isn't used more than 500 times in quick succession
	next_avaliable -> TIME	# holds the next avaliable time a proxy can be used

	gt_last_updated -> TIME	# holds a guest token (a token used when making a request. It can be invalid.)
	success_delta -> 0 LIMIT ( 5, 000 -> 10, 000 ) # holds information on how many times a proxy was successful
	failed_count -> 0 (0 -> 100) # holds information on the number of concequative failed attemps 
```

```

add_proxy_to_queue():	# proxies are added into a queue, and then used at a later point

	# makes a query to the database to get a free proxy using parameters.
	proxy = get_proxy(get a proxy where the next_avaliable time is now, sort by success_delta)

	if(there are proxies avaliable):

		if(usage_count > 500):	# checks if the proxy has been used more than 500 times recently

			# we are close to the rate limit of the proxy

			usage_count = 0	# reset the usage count
			failed_count = 0 # reset the failed count (should this be done here?)

			next_avaliable = time + 15 minutes	# up the next_avaliable time to 15 minutes into the future.

			<RETURN NEW PROXY>	# get a new proxy, since this one can not be used
			(END)

		if(failed_count > 100): # check if there have been over 100 sequential failed requests

			usage_count = 0	# reset the usage count	(should this be done here?)
			failed_count = 0	# reset the failed count
			next_avaliable = time + 10 minutes	# up the next_avaliable time to 15 minutes into the future.

			<RETURN NEW PROXY>	# get a new proxy, since this one can not be used
			(END)

		+1 usage count	# add 1 to the usage count

		make_regular_request()

		if(HTTP request success && Proxy is working):	# if the HTTP request was a success using proxy
			+ 1 success_delta	# include the success delta
			failed_count = 0 # reset the consecutive fail count {p1}
			if(guest_token_invalid):	# if the Guest token (GT) was invalid
				if(gt_last_updated is longer than 2 minutes ago):	# and hasn't been updated recently
					make_gt_request()	# make a separate request to get a new guest token
					update_gt()	# update the guest token
				next_avaliable = 0	# allow this proxy to be used again
			else:
				save_reqest()	# if the guest token was correct, and the HTTP request didn't fail due to a proxy
		else:
			-1 success_delta	# decrease the success delta
			+1 failed_count	# increase the consecutive fail count
			next_avaliable = NOW() + 5 minutes	# make the proxy usable again in 5 minutes

```

Additional suggestions:
- Use a ML model to choose which proxy to use.
- Backoff strategy 2^n seconds timeout. n = consecutive filed attempts -> max n + $r (randomness).
- Use a rolling time window instead



























