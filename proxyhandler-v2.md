get_new_proxy():
    
    proxy = get_proxy_query() # where next_avaliable is in the future, sort by success_delta
    
    if(proxy != null):

        if(proxy.usage_count > 500):
            update_proxy(proxy, {
                usage_count: 0,
                failed_count: 0,
                next_avaliable: NOW() + 15 minutes
            })
            return get_new_proxy()

        if(proxy.failed_count > 100):
            update_proxy(proxy, {
                usage_count: 0,
                failed_count: 0,
                next_avaliable: NOW() + 10 minutes
            })

            return get_new_proxy()

        proxy.usage_count += 1

        response = make_request("make search request", proxy)

        if(response.success is True):
            proxy.success_delta += 1
            proxy.failed_count = 0
            if(response.guest_token_invalid == true):
                if(proxy.guest_token_last_updated more than 2 minutes ago):
                    response2 = make_request("make guest token request", proxy)
                    update_proxy({
                        guest_token: response2.guest_token
                    })
                    proxy.next_avliable = 0
                else:
                    save_response_to_db(response)
        else:
            proxy.success_delta -= 1
            proxy.failed_count += 1
